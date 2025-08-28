import textwrap
import logging
import datetime  # 新增：用于正确的时间格式化
from pathlib import Path
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict
from crawl4ai import AsyncWebCrawler

import langextract as lx
from langextract import data

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="CrawlLangAI API", version="1.0.0")

# 确保输出目录存在
OUTPUT_DIR = Path("extraction_results")
OUTPUT_DIR.mkdir(exist_ok=True)


class CrawlRequest(BaseModel):
    url: str
    timeout: Optional[int] = 30


def format_extractions(extractions: List[lx.data.Extraction]) -> List[Dict]:
    """
    将提取结果格式化为纯Python原生字典，只保留核心业务字段
    """
    formatted = []
    for ext in extractions:
        # 跳过属性不完整的提取结果
        if not hasattr(ext, "attributes"):
            continue

        attrs = ext.attributes
        # 严格校验核心字段（标题、链接、日期）
        required_fields = ["announcement", "link", "date"]
        if not all(field in attrs for field in required_fields):
            continue

        # 清理字段值（去空格、处理空值）
        announcement = attrs["announcement"].strip() if isinstance(attrs["announcement"], str) else ""
        link = attrs["link"].strip() if isinstance(attrs["link"], str) else ""
        date = attrs["date"].strip() if isinstance(attrs["date"], str) else None

        # 过滤无效数据
        if not announcement or not link:
            continue
        if date and len(date) != 10:  # 简单校验YYYY-MM-DD格式长度
            date = None

        formatted.append({
            "announcement": announcement,
            "link": link,
            "date": date
        })

    # 去重处理
    seen = set()
    unique_results = []
    for item in formatted:
        unique_key = f"{item['announcement']}|{item['link']}"
        if unique_key not in seen:
            seen.add(unique_key)
            unique_results.append(item)

    return unique_results


@app.post("/crawl-and-extract")
async def crawl_and_extract(request: CrawlRequest):
    """
    完整流程：爬取网页 → 提取公告 → 格式化结果
    """
    try:
        # 1. 爬取网页内容
        logger.info(f"开始爬取网页: {request.url}")
        async with AsyncWebCrawler() as crawler:
            crawl_result = await crawler.arun(
                url=request.url,
                timeout=request.timeout
            )

            # 校验爬取结果
            if not hasattr(crawl_result, "markdown") or not crawl_result.markdown:
                raise HTTPException(status_code=400, detail="爬取结果为空，未获取到文本内容")

            markdown_content = crawl_result.markdown
            logger.info(markdown_content)
            logger.info(f"爬取成功，文本长度: {len(markdown_content)} 字符")

        # 2. 定义提取规则
        prompt_description = textwrap.dedent("""\
            任务：政府采购网页文本中提取公告信息，公告信息必须包含标题、链接、日期。
            公告标题必须是采购意向公告的标题，不能是其他类型的公告标题。
            公告必须含有日期，没有日期的不要。
            若公告未没有日期，不是公告信息，不要提取。
            必须返回3个字段，且仅返回这3个字段：
            1. announcement：公告标题（原始文本，去多余空格，不含日期）
            2. link：公告链接（必须以http://或https://开头）
            3. date：发布日期（格式YYYY-MM-DD，没有日期的不是公告信息）
        """)

        # 3. 构造示例数据
        structured_examples = [
            lx.data.ExampleData(
                text=textwrap.dedent("""\
 * **·** [天津市人民医院政府采购意向公告](http://www.ccgp-tianjin.gov.cn/viewer.do?id=775605293&ver=2 "天津市人民医院政府采购意向公告")2025-08-28
                """),
                extractions=[
                    lx.data.Extraction(
                        extraction_class="procurement_announcement",
                        extraction_text="天津市人民医院政府采购意向公告",
                        attributes={
                            "announcement": "天津市人民医院政府采购意向公告",
                            "link": "http://www.ccgp-tianjin.gov.cn/viewer.do?id=775605293&ver=2",
                            "date": "2025-08-28"
                        }
                    )
                ]
            ),
            lx.data.ExampleData(
                text=textwrap.dedent("""\
                     * **·** [天津市数据局机关政府采购意向公告](http://www.ccgp-tianjin.gov.cn/viewer.do?id=775122615&ver=2 "天津市数据局机关政府采购意向公告")2025-08-27
                """),
                extractions=[
                    lx.data.Extraction(
                        extraction_class="procurement_announcement",
                        extraction_text="天津市数据局机关政府采购意向公告",
                        attributes={
                            "announcement": "天津市数据局机关政府采购意向公告",
                            "link": "http://www.ccgp-tianjin.gov.cn/viewer.do?id=775122615&ver=2",
                            "date": "2025-08-27"
                        }
                    )
                ]
            )
        ]

        # 4. 调用langextract提取
        logger.info("开始提取政府采购公告信息")
        try:
            extraction_result = lx.extract(
                text_or_documents=markdown_content,
                prompt_description=prompt_description,
                examples=structured_examples,
                model_id="qwen3:4b",
                model_url="http://192.168.0.199:11434",
                temperature=0.1,
                extraction_passes=1,
                max_char_buffer=2000,
                use_schema_constraints=True,
                debug=False
            )

            # 5. 格式化结果
            formatted_data = format_extractions(extraction_result.extractions)
            logger.info(f"提取完成，共获取 {len(formatted_data)} 条有效公告")

            # 6. 保存结果到文本文件（修复时间格式化问题）
            if formatted_data:
                output_file = OUTPUT_DIR / "procurement_results.txt"
                # 获取当前时间并格式化（使用datetime替代logging格式化器）
                current_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

                with open(output_file, "w", encoding="utf-8") as f:
                    f.write(f"爬取URL: {request.url}\n")
                    f.write(f"提取时间: {current_time}\n")  # 使用正确格式化的时间
                    f.write(f"有效公告数: {len(formatted_data)}\n\n")
                    for idx, item in enumerate(formatted_data, 1):
                        f.write(f"【公告{idx}】\n")
                        f.write(f"标题: {item['announcement']}\n")
                        f.write(f"链接: {item['link']}\n")
                        f.write(f"日期: {item['date'] or '未获取到'}\n\n")

            # 7. 返回结果
            return {
                "success": True,
                "url": request.url,
                "extraction_count": len(formatted_data),
                "announcements": formatted_data,
                "debug_info": {
                    "raw_text_length": len(markdown_content),
                    "saved_file": str(output_file) if formatted_data else None
                }
            }

        except lx.resolver.ResolverParsingError as e:
            logger.error(f"模型输出格式错误: {str(e)}", exc_info=True)
            raise HTTPException(status_code=500, detail="模型未按规则输出，请检查提示词或示例数据")
        except Exception as e:
            logger.error(f"提取过程失败: {str(e)}", exc_info=True)
            raise HTTPException(status_code=500, detail=f"提取异常: {str(e)}")

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"整体流程失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"系统错误: {str(e)}")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="info")
