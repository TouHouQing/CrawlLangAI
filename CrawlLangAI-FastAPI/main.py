import logging
import re
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict
from crawl4ai import AsyncWebCrawler

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="CrawlLangAI API", version="1.0.0")

class CrawlRequest(BaseModel):
    url: str
    timeout: Optional[int] = 30


def extract_procurement_announcements(text: str) -> List[Dict]:
    """
    使用正则表达式从文本中提取政府采购公告信息
    """
    # 匹配公告的正则表达式
    pattern = r'\*\s*\*\*·\*\*\s*\[([^\]]+)\]\(([^\)]+)\s*"[^"]+"\)\s*(\d{4}-\d{2}-\d{2})'
    matches = re.findall(pattern, text)
    
    announcements = []
    for match in matches:
        announcement, link, date = match
        # 清理字段值（去空格、处理空值）
        announcement = announcement.strip()
        link = link.strip()
        date = date.strip() if date else None
        
        # 过滤无效数据
        if not announcement or not link:
            continue
        
        announcements.append({
            "announcement": announcement,
            "link": link,
            "date": date
        })
    
    # 去重处理
    seen = set()
    unique_results = []
    for item in announcements:
        unique_key = f"{item['announcement']}|{item['link']}"
        if unique_key not in seen:
            seen.add(unique_key)
            unique_results.append(item)
    
    return unique_results


@app.post("/crawl")
async def crawlt(request: CrawlRequest):
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

        # 2. 使用正则表达式提取公告信息
        logger.info("开始提取政府采购公告信息")
        formatted_data = extract_procurement_announcements(markdown_content)
        logger.info(f"提取完成，共获取 {len(formatted_data)} 条有效公告")

        # 3. 返回结果
        return {
            "success": True,
            "url": request.url,
            "total": len(formatted_data),
            "announcements": formatted_data,
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"整体流程失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"系统错误: {str(e)}")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="info")