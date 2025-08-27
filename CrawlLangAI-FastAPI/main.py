import textwrap

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict
from crawl4ai import AsyncWebCrawler

import langextract as lx

app = FastAPI(title="CrawlLangAI API", version="1.0.0")

class CrawlRequest(BaseModel):
    url: str
    timeout: Optional[int] = 30
    
class ExtractRequest(BaseModel):
    text: str
    max_length: Optional[int] = 200
    
class StructuredExtractRequest(BaseModel):
    text: str
    extract_type: str = "announcements"

@app.post("/crawl-and-extract")
async def crawl_and_extract(request: CrawlRequest):
    """
    完整的爬取和提取流程，使用langextract处理文本信息，结构化返回公告、链接、日期
    """
    try:
        # 第一步：使用crawl4ai的AsyncWebCrawler爬取网页
        async with AsyncWebCrawler() as crawler:
            result = await crawler.arun(
                url=request.url,
                timeout=request.timeout
            )
            markdown_content = result.markdown

            # 第二步：使用langextract提取结构化数据
            prompt_description = """
            任务：从输入文本中提取所有“政府采购意向公告”。
            要求：为每条公告抽取以下属性，严格按键名输出：
            - announcement：公告标题（去除多余空格与标点，不包含日期与引号）
            - link：公告链接（http:// 或 https:// 开头，若存在多个仅保留与 viewer.do 相关的链接）
            - date：公告发布日期（YYYY-MM-DD）
            规则：
            - 仅在三项信息都能确定时输出该条记录；
            - 不虚构；无法确定则跳过；
            - 不要返回除上述三键以外的其他键。
            """

            # 使用langextract提取结构化数据
            try:
                structured_examples = [
                    lx.data.ExampleData(
                        text=textwrap.dedent("""\
                            announcement: 天津铁道职业技术学院政府采购意向公告
                            link: http://www.ccgp-tianjin.gov.cn/viewer.do?id=775085171&ver=2
                            date: 2025-08-27"""),
                        extractions=[
                            lx.data.Extraction(
                                extraction_class="organization",
                                extraction_text="天津铁道职业技术学院",
                                attributes={"type": "educational institution"}
                            ),
                            lx.data.Extraction(
                                extraction_class="document_type",
                                extraction_text="政府采购意向公告",
                                attributes={}
                            ),
                            lx.data.Extraction(
                                extraction_class="url",
                                extraction_text="http://www.ccgp-tianjin.gov.cn/viewer.do?id=775085171&ver=2",
                                attributes={"source": "Tianjin government procurement platform"}
                            ),
                            lx.data.Extraction(
                                extraction_class="date",
                                extraction_text="2025-08-27",
                                attributes={"format": "YYYY-MM-DD", "type": "announcement date"}
                            )
                        ]
                    ),
                    lx.data.ExampleData(
                        text=textwrap.dedent("""\
                            announcement: 天津市第一中心医院政府采购意向公告
                            link: http://www.ccgp-tianjin.gov.cn/viewer.do?id=775015755&ver=2
                            date: 2025-08-27"""),
                        extractions=[
                            lx.data.Extraction(
                                extraction_class="organization",
                                extraction_text="天津市第一中心医院",
                                attributes={"type": "medical institution"}
                            ),
                            lx.data.Extraction(
                                extraction_class="document_type",
                                extraction_text="政府采购意向公告",
                                attributes={}
                            ),
                            lx.data.Extraction(
                                extraction_class="url",
                                extraction_text="http://www.ccgp-tianjin.gov.cn/viewer.do?id=775015755&ver=2",
                                attributes={"source": "Tianjin government procurement platform"}
                            ),
                            lx.data.Extraction(
                                extraction_class="date",
                                extraction_text="2025-08-27",
                                attributes={"format": "YYYY-MM-DD", "type": "announcement date"}
                            )
                        ]
                    ),
                    lx.data.ExampleData(
                        text=textwrap.dedent("""\
                            announcement: 天津市野生动物救护驯养繁殖中心政府采购意向公告
                            link: http://www.ccgp-tianjin.gov.cn/viewer.do?id=774372583&ver=2
                            date: 2025-08-26"""),
                        extractions=[
                            lx.data.Extraction(
                                extraction_class="organization",
                                extraction_text="天津市野生动物救护驯养繁殖中心",
                                attributes={"type": "wildlife protection institution"}
                            ),
                            lx.data.Extraction(
                                extraction_class="document_type",
                                extraction_text="政府采购意向公告",
                                attributes={}
                            ),
                            lx.data.Extraction(
                                extraction_class="url",
                                extraction_text="http://www.ccgp-tianjin.gov.cn/viewer.do?id=774372583&ver=2",
                                attributes={"source": "Tianjin government procurement platform"}
                            ),
                            lx.data.Extraction(
                                extraction_class="date",
                                extraction_text="2025-08-26",
                                attributes={"format": "YYYY-MM-DD", "type": "announcement date"}
                            )
                        ]
                    ),
                    lx.data.ExampleData(
                        text=textwrap.dedent("""\
                            announcement: 天津轻工职业技术学院政府采购意向公告
                            link: http://www.ccgp-tianjin.gov.cn/viewer.do?id=774271865&ver=2
                            date: 2025-08-26"""),
                        extractions=[
                            lx.data.Extraction(
                                extraction_class="organization",
                                extraction_text="天津轻工职业技术学院",
                                attributes={"type": "educational institution"}
                            ),
                            lx.data.Extraction(
                                extraction_class="document_type",
                                extraction_text="政府采购意向公告",
                                attributes={}
                            ),
                            lx.data.Extraction(
                                extraction_class="url",
                                extraction_text="http://www.ccgp-tianjin.gov.cn/viewer.do?id=774271865&ver=2",
                                attributes={"source": "Tianjin government procurement platform"}
                            ),
                            lx.data.Extraction(
                                extraction_class="date",
                                extraction_text="2025-08-26",
                                attributes={"format": "YYYY-MM-DD", "type": "announcement date"}
                            )
                        ]
                    )
                ]
                
                structured_data = lx.extract(
                    text_or_documents=markdown_content,
                    prompt_description=prompt_description,
                    model_id="gemma3:1b",
                    model_url="http://192.168.0.115:11434",
                    fence_output=False,
                    use_schema_constraints=False,
                    examples=structured_examples
                )

                # 解析langextract结果
                langextract_announcements = []
                if structured_data and hasattr(structured_data, 'extractions'):
                    for extraction in structured_data.extractions:
                        if extraction.attributes:
                            announcement = {
                                "announcement": extraction.attributes.get('announcement', extraction.extraction_text),
                                "link": extraction.attributes.get('link', ''),
                                "date": extraction.attributes.get('date', '')
                            }
                            langextract_announcements.append(announcement)
                        else:
                            # 如果没有属性，尝试从extraction_text中解析
                            langextract_announcements.extend(parse_extraction_text(extraction.extraction_text))

                if langextract_announcements:
                    return {
                        "success": True,
                        "url": request.url,
                        "method": "langextract",
                        "data": langextract_announcements,
                        "count": len(langextract_announcements),
                        "raw_markdown_length": len(markdown_content)
                    }

            except Exception as langextract_error:
                # langextract失败，继续处理
                pass

            # 如果都没有找到公告信息，返回错误
            raise HTTPException(status_code=404, detail="未找到公告信息")

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"处理失败: {str(e)}")

def parse_extraction_text(extraction_text: str) -> List[Dict]:
    """
    从extraction_text中解析公告信息
    """
    announcements = []
    # 尝试简单的文本解析逻辑
    lines = extraction_text.split('\n')
    for line in lines:
        line = line.strip()
        if line and ('公告' in line or 'http' in line):
            # 这里可以添加更复杂的解析逻辑
            announcements.append({
                "announcement": line,
                "link": "",
                "date": ""
            })
    return announcements

def extract_key_points(text: str, max_length: int = 200) -> str:
    """
    提取文本的关键信息
    """
    # 简单的文本处理：清理多余空白并截断
    cleaned_text = ' '.join(text.strip().split())

    if len(cleaned_text) <= max_length:
        return cleaned_text

    # 在不使用正则的情况下，尽量在常见标点处截断
    cutoff = max_length
    preferred_punctuations = ['。', '！', '？', '.', '!', '?']
    search_window = cleaned_text[:max_length]
    last_punct_index = -1
    for p in preferred_punctuations:
        index = search_window.rfind(p)
        if index > last_punct_index:
            last_punct_index = index
    if last_punct_index != -1 and last_punct_index + 1 >= int(max_length * 0.5):
        cutoff = last_punct_index + 1
    return (cleaned_text[:cutoff]).strip() + ("" if cutoff == len(cleaned_text) else "...")

    

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)