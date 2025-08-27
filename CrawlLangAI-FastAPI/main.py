from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional
from crawl4ai import AsyncWebCrawler
import asyncio
from bs4 import BeautifulSoup
import re

app = FastAPI(title="CrawlLangAI API", version="1.0.0")

class CrawlRequest(BaseModel):
    url: str
    timeout: Optional[int] = 30
    
class ExtractRequest(BaseModel):
    text: str
    max_length: Optional[int] = 200

@app.post("/crawl")
async def crawl_website(request: CrawlRequest):
    """
    爬取网页并提取为markdown格式
    """
    try:
        # 使用crawl4ai的AsyncWebCrawler爬取网页
        async with AsyncWebCrawler() as crawler:
            result = await crawler.arun(
                url=request.url,
                timeout=request.timeout
            )
            
            # 提取markdown内容
            markdown_content = result.markdown
            
            return {
                "success": True,
                "url": request.url,
                "markdown": markdown_content,
                "length": len(markdown_content)
            }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"爬取失败: {str(e)}")

@app.post("/extract")
async def extract_text(request: ExtractRequest):
    """
    使用langextract处理文本信息
    """
    try:
        # 使用文本处理提取关键信息
        extracted_info = extract_key_points(
            text=request.text,
            max_length=request.max_length
        )
        
        return {
            "success": True,
            "original_length": len(request.text),
            "extracted_info": extracted_info,
            "extracted_length": len(extracted_info)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"文本提取失败: {str(e)}")

@app.post("/crawl-and-extract")
async def crawl_and_extract(request: CrawlRequest):
    """
    完整的爬取和提取流程
    """
    try:
        # 第一步：使用crawl4ai的AsyncWebCrawler爬取网页
        async with AsyncWebCrawler() as crawler:
            result = await crawler.arun(
                url=request.url,
                timeout=request.timeout
            )
            markdown_content = result.markdown
            
            # 第二步：提取关键信息
            extracted_info = extract_key_points(
                text=markdown_content,
                max_length=200
            )
            
            return {
                "success": True,
                "url": request.url,
                "markdown_length": len(markdown_content),
                "extracted_info": extracted_info,
                "extracted_length": len(extracted_info)
            }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"处理失败: {str(e)}")

@app.get("/")
async def root():
    return {
        "message": "CrawlLangAI API 服务运行中",
        "endpoints": [
            "POST /crawl - 爬取网页并提取markdown",
            "POST /extract - 处理文本信息",
            "POST /crawl-and-extract - 完整的爬取和提取流程"
        ]
    }

def extract_key_points(text: str, max_length: int = 200) -> str:
    """
    提取文本的关键信息
    """
    # 简单的文本处理：提取前N个字符并清理格式
    cleaned_text = re.sub(r'\s+', ' ', text.strip())
    
    if len(cleaned_text) <= max_length:
        return cleaned_text
    
    # 尝试找到句子边界进行截断
    sentences = re.split(r'([.!?。！？]+)', cleaned_text)
    result = ""
    
    for i in range(0, len(sentences), 2):
        if i + 1 < len(sentences):
            sentence = sentences[i] + sentences[i+1]
        else:
            sentence = sentences[i]
            
        if len(result + sentence) > max_length:
            break
        result += sentence
    
    return result.strip() or cleaned_text[:max_length].strip() + "..."

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)