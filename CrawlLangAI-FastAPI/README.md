# CrawlLangAI FastAPI 服务

一个基于 FastAPI 的网页爬取和文本处理服务，使用 crawl4ai 和 langextract 库。

## 功能特性

- 🕷️ 使用 crawl4ai 爬取网页并提取为 markdown 格式
- 📝 使用 langextract 处理文本信息，提取关键内容
- 🔄 完整的爬取和提取一体化流程
- 🚀 基于 FastAPI 的高性能 API 服务

## 安装依赖

```bash
pip install -r requirements.txt
```

## 启动服务

```bash
python main.py
```

服务将在 http://localhost:8000 启动

## API 端点

### 1. 根路径
- **GET** `/` - 获取服务信息和可用端点

### 2. 网页爬取
- **POST** `/crawl` - 爬取指定网页并返回 markdown 内容
  - 请求体: `{"url": "https://example.com", "timeout": 30}`

### 3. 文本提取
- **POST** `/extract` - 处理文本信息并提取关键内容
  - 请求体: `{"text": "长文本内容...", "max_length": 200}`

### 4. 完整流程
- **POST** `/crawl-and-extract` - 完整的爬取和提取流程
  - 请求体: `{"url": "https://example.com", "timeout": 30}`

## 使用示例

### 使用 curl 测试

```bash
# 获取服务信息
curl http://localhost:8000/

# 爬取网页
curl -X POST http://localhost:8000/crawl \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com"}'

# 处理文本
curl -X POST http://localhost:8000/extract \
  -H "Content-Type: application/json" \
  -d '{"text": "这是一段需要处理的文本内容..."}'
```

### 使用 Python requests

```python
import requests
import json

# 爬取网页
response = requests.post(
    "http://localhost:8000/crawl",
    json={"url": "https://example.com"}
)
print(response.json())

# 处理文本
response = requests.post(
    "http://localhost:8000/extract", 
    json={"text": "你的文本内容..."}
)
print(response.json())
```

## 配置参数

### CrawlRequest 参数
- `url` (必需): 要爬取的网页 URL
- `timeout` (可选): 超时时间，默认 30 秒

### ExtractRequest 参数  
- `text` (必需): 要处理的文本内容
- `max_length` (可选): 提取内容的最大长度，默认 200 字符

## 开发说明

项目结构:
```
CrawlLangAI-FastAPI/
├── main.py          # 主应用文件
├── requirements.txt # 依赖文件
└── README.md        # 说明文档
```

## 注意事项

- 确保网络连接正常以便爬取网页
- 遵守目标网站的 robots.txt 协议
- 合理设置超时时间避免长时间等待