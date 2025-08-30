import logging
import re
from fastapi import FastAPI, HTTPException
from typing import Optional, List, Dict
from crawl4ai import AsyncWebCrawler

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="CrawlLangAI API", version="1.0.0")


def extract_procurement_announcements(text: str) -> List[Dict]:
    """
    使用正则表达式从文本中提取政府采购公告信息
    """
    # 匹配公告的正则表达式
    pattern = r'\*\s*\*\*·\*\*\s*\[([^\]]+)\]\(([^\)]+)\s*"[^"]+"\)\s*(\d{4}-\d{2}-\d{2})'
    matches = re.findall(pattern, text)
    
    announcements = []
    for match in matches:
        full_title, link, date = match
        # 清理字段值（去空格、处理空值）
        full_title = full_title.strip()
        link = link.strip()
        date = date.strip() if date else None
        
        # 提取项目编号
        project_number = None
        project_number_match = re.search(r'\(项目编号:([^)]+)\)', full_title)
        if project_number_match:
            project_number = project_number_match.group(1).strip()
        
        # 提取公告标题（去除项目编号部分）
        announcement = re.sub(r'\s*\(项目编号:[^)]+\)', '', full_title).strip()
        
        # 过滤无效数据
        if not announcement or not link:
            continue
        
        announcements.append({
            "announcement": announcement,
            "link": link,
            "date": date,
            "project_number": project_number
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


@app.get("/crawl_Tenders")
async def crawl_tenders(url: str, timeout: Optional[int] = 30):
    """
    完整流程：爬取网页 → 提取公告 → 格式化结果
    """
    try:
        # 1. 爬取网页内容
        logger.info(f"开始爬取网页: {url}")
        async with AsyncWebCrawler() as crawler:
            crawl_result = await crawler.arun(
                url=url,
                timeout=timeout
            )

            # 校验爬取结果
            if not hasattr(crawl_result, "markdown") or not crawl_result.markdown:
                raise HTTPException(status_code=400, detail="爬取结果为空，未获取到文本内容")

            markdown_content = crawl_result.markdown

        formatted_data = extract_procurement_announcements(markdown_content)

        # 3. 返回结果
        return {
            "success": True,
            "url": url,
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