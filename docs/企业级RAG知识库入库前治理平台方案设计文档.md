# 企业级 RAG 知识库入库前治理平台方案设计文档

## 1. 文档说明

本文档面向企业级 RAG 知识库入库前治理平台，重点说明系统的总体架构、技术选型、服务拆分、核心流程、数据存储、任务状态、文档解析、分片、向量化、索引、审核发布、召回测试、日志监控和开发阶段规划。

本文档不把项目设计成简单 Demo，而是按照后续可扩展、可治理、可追溯、可运维的企业级项目进行设计。

本项目第一阶段目标不是完整聊天机器人，而是先完成 **RAG 知识入库前治理完整闭环**：

```text
知识库管理
→ 目录管理
→ 文档上传
→ 文档解析
→ 结构识别
→ 分片生成
→ 向量化
→ ES / Milvus 入库
→ 分片预览
→ 召回测试
→ 审核发布
→ 正式问答可命中
```

---

## 2. 项目定位

### 2.1 项目目标

本项目定位为企业级 RAG 知识库入库前治理平台，主要解决企业内部知识文档在进入正式 RAG 问答系统前的治理问题。

核心目标包括：

1. 支持知识库和目录化管理。
2. 支持多格式文档上传和校验。
3. 支持普通 PDF、扫描 PDF、Word、Excel、Markdown、TXT 等文档解析。
4. 支持 Parser Router 多解析器路由。
5. 支持结构化分片，而不是简单按长度切分。
6. 支持 bge-m3 向量化。
7. 支持 Elasticsearch 存储 chunk 正文和全文检索。
8. 支持 Milvus 存储 chunk 向量。
9. 支持发布前召回测试。
10. 支持轻量审核发布。
11. 支持下架、重新发布、重新处理、版本追溯。

### 2.2 第一阶段范围

第一阶段采用完整 RAG 入库闭环方案，包含：

```text
知识库管理
目录管理
文档上传
文档解析
结构识别
分片生成
Embedding 向量化
ES 索引写入
Milvus 向量写入
召回测试
审核发布
任务进度展示
```

第一阶段默认不启用：

```text
BM25 + 向量混合召回
Rerank 重排
复杂权限
多租户
复杂工作流审批
完整 Agent 工具调用
```

但这些能力需要在架构上预留扩展点。

---

## 3. 总体架构

### 3.1 总体架构分层

系统整体分为四层：

```text
前端层
  └── frontend/web

Java 业务服务层
  ├── gateway-service
  ├── system-service
  └── chat-service

Python AI 能力层
  ├── document-process-service
  ├── visual-parser-service
  ├── ai-model-service
  └── ai-agent-service

中间件与基础设施层
  ├── Nacos
  ├── MySQL
  ├── Redis
  ├── RabbitMQ
  ├── MinIO
  ├── Elasticsearch 8.x + IK
  └── Milvus
```

### 3.2 核心职责划分

```text
Java：业务主控、状态管理、用户入口、审核发布、任务调度
Python：文档处理、视觉解析、模型调用、AI 编排、索引写入
MySQL：元数据、状态、配置、审核、发布、任务记录
MinIO：原始文件和处理产物归档
Elasticsearch：chunk 正文、分片预览、全文检索、BM25 预留
Milvus：向量索引和向量召回
RabbitMQ：Java 与 Python 的异步任务流转
Redis：缓存、任务进度、幂等、锁、限流、会话临时上下文
```

### 3.3 总体调用链路

#### 文档入库链路

```text
前端
→ gateway-service
→ system-service
→ MinIO 保存原始文件
→ MySQL 保存元数据
→ RabbitMQ 发送文档处理任务
→ document-process-service 消费任务
→ visual-parser-service 处理扫描 PDF / 图片 PDF
→ ai-model-service 生成 Embedding
→ document-process-service 写 MinIO / ES / Milvus
→ document-process-service 回调 system-service
→ system-service 更新任务状态
→ 前端通过 SSE 查看处理进度
```

#### 召回测试链路

```text
前端召回测试页面
→ gateway-service
→ system-service
→ ai-agent-service
→ ai-model-service 生成 query embedding
→ Milvus TopK 向量召回
→ Elasticsearch 批量补全 chunk 正文
→ 返回命中分片、来源、页码、相似度
```

#### 正式聊天问答链路

```text
前端聊天页面
→ gateway-service
→ chat-service
→ ai-agent-service
→ ai-model-service / Milvus / Elasticsearch
→ chat-service 保存消息
→ 前端 SSE 流式返回
```

---

## 4. 服务拆分设计

## 4.1 Java 服务

### 4.1.1 gateway-service

统一前端入口，负责：

```text
统一路由
统一鉴权入口
跨域处理
限流预留
traceId 生成和透传
请求日志
转发 system-service / chat-service
```

### 4.1.2 system-service

后台系统和知识库治理主服务，负责：

```text
用户 / 角色 / 简单权限
知识库管理
目录管理
文档管理
文件上传
file_hash 去重
MinIO 原始文件元数据维护
分片配置管理
文档版本管理
文档处理任务创建
RabbitMQ 消息发送
任务状态管理
Python 回调接收
分片元数据管理
审核发布
下架 / 重新发布
召回测试入口
任务进度 SSE
操作日志
系统配置
```

### 4.1.3 chat-service

用户聊天主控服务，负责：

```text
单聊
多轮会话
聊天消息保存
上下文管理
普通大模型问答入口
知识库问答入口
调用 ai-agent-service
SSE 流式输出
WebSocket 转人工预留
人工客服会话状态预留
```

### 4.1.4 common

公共 jar 模块，不单独部署。负责：

```text
统一返回 Result
统一错误码
业务异常
分页对象
基础枚举
JWT 工具
登录用户上下文
MDC traceId 工具
RabbitMQ 消息基类
Redis 工具封装
MinIO 工具封装
MyBatis-Plus 公共配置
参数校验公共处理
```

common 不能放业务逻辑，不能变成垃圾桶。

---

## 4.2 Python 服务

### 4.2.1 document-process-service

文档处理主流程服务，负责：

```text
消费 RabbitMQ 文档处理任务
执行 Parser Router
判断文档类型和解析策略
调用 visual-parser-service 处理扫描 PDF / 图片 PDF
统一转换 DocumentStructure
执行不同文档类型的 ChunkStrategy
调用 ai-model-service 生成向量
写 Elasticsearch
写 Milvus
归档处理产物到 MinIO
回调 system-service 更新状态
```

### 4.2.2 visual-parser-service

公司内部影像解析服务，负责：

```text
扫描 PDF 解析
图片型 PDF 解析
图片 OCR
复杂版面识别
表格结构识别
图片块内容提取
第一阶段内部使用 MinerU
后续扩展 PaddleOCR / PP-Structure / VLM
```

该服务不负责任务状态、不负责分片、不负责写 ES/Milvus。

### 4.2.3 ai-model-service

统一模型网关服务，负责：

```text
Embedding 模型调用
LLM 模型调用
Rerank 模型调用预留
多 Provider 路由
模型配置管理
超时控制
重试控制
调用日志
成本统计预留
```

第一阶段实现：

```text
bge-m3 Embedding
通义千问 Provider 预留
智谱 GLM Provider 预留
Ollama Provider 预留
OpenAI-Compatible Provider 预留
```

模型不写死，通过配置文件控制具体启用哪个 Provider。

### 4.2.4 ai-agent-service

AI 编排服务，由 Java system-service / chat-service 调用，负责：

```text
召回测试执行
意图识别预留
问题改写预留
RAG 检索编排
Milvus 向量召回
ES 批量补全文本
BM25 预留
RRF 融合预留
Rerank 预留
Prompt 组装
调用 ai-model-service 生成回答
后续 Agent 工具调用编排
```

---

## 5. 技术选型

## 5.1 Java 技术栈

```text
JDK 21
Spring Boot 3.x
Spring Cloud
Spring Cloud Gateway
Spring Cloud OpenFeign
Spring Security + JWT
MyBatis-Plus
MySQL 8.x
Redis + Redisson
RabbitMQ
MinIO Java SDK
Elasticsearch Java Client
Knife4j + OpenAPI 3
MapStruct
Lombok
Hibernate Validator
Flyway
Maven 多模块
```

### 5.1.1 Java 规范要求

Java 代码严格按照阿里开发规范：

```text
类名使用 UpperCamelCase
方法名、变量名使用 lowerCamelCase
常量使用 UPPER_UNDERSCORE_CASE
禁止魔法值
禁止 Controller 写业务逻辑
禁止 Entity 直接返回前端
禁止使用 Map 代替明确 DTO
统一异常处理
统一错误码
统一返回结构
分页查询必须限制 pageSize
数据库字段必须有 create_time / update_time
重要表使用逻辑删除
```

### 5.1.2 Java 分层建议

每个业务服务按以下结构分层：

```text
service-name/
├── xxx-api
├── xxx-application
├── xxx-domain
├── xxx-infrastructure
└── xxx-starter
```

职责：

```text
api：Controller、Request、Response、VO
application：应用服务、事务编排、Command、DTO
domain：领域对象、领域服务、枚举、业务规则
infrastructure：Mapper、Entity、外部接口、MQ、Redis、MinIO
starter：启动类、配置类
```

---

## 5.2 Python 技术栈

```text
Python 3.11
FastAPI
Uvicorn / Gunicorn
Pydantic v2
RabbitMQ Client：aio-pika / pika
Redis-py
MinIO Python SDK
Elasticsearch Python Client
pymilvus
sentence-transformers / FlagEmbedding
PyMuPDF
pdfplumber
Docling
MinerU
python-docx
openpyxl
markdown-it-py
pandas
structlog / loguru
```

### 5.2.1 Python 规范要求

Python 代码要求：

```text
使用类型注解
使用 Pydantic 定义请求和响应模型
服务分层清晰
Parser / Chunker / Model Provider 插件化
禁止把业务逻辑写进 Controller
禁止函数过长
重要配置全部从配置文件读取
日志必须带 traceId
异常必须转换为统一错误响应
```

### 5.2.2 Python 分层建议

```text
service-name/
├── api/
├── application/
├── domain/
├── infrastructure/
├── config/
├── common/
└── main.py
```

---

## 5.3 前端技术栈

前端先使用一个工程，不拆多个前端项目。

```text
Vue 3
TypeScript
Vite
Element Plus
Pinia
Vue Router
Axios
ECharts
```

目录结构：

```text
frontend/
└── web/
    ├── src/
    │   ├── views/
    │   │   ├── admin/
    │   │   └── chat/
    │   ├── api/
    │   ├── router/
    │   ├── store/
    │   ├── components/
    │   └── utils/
    └── package.json
```

---

## 6. 中间件选型

| 中间件 | 选型 | 用途 |
|---|---|---|
| 注册 / 配置中心 | Nacos | Java 服务注册发现、配置中心 |
| 数据库 | MySQL 8.x | 元数据、任务、状态、审核发布 |
| 缓存 | Redis | 缓存、锁、幂等、进度、限流、会话临时上下文 |
| 消息队列 | RabbitMQ | Java 与 Python 异步任务流转 |
| 对象存储 | MinIO | 原始文件、解析产物、分片产物、处理报告归档 |
| 全文检索 | Elasticsearch 8.x + IK | chunk 正文、分片预览、BM25 预留 |
| 向量库 | Milvus | chunk 向量、向量召回 |

说明：

```text
Nacos 只要求 Java 服务注册。
Python 服务不强制注册 Nacos。
Python 服务地址通过 Nacos 配置、Docker Compose 服务名或 K8s Service 域名维护。
```

---

## 7. 文档入库处理流程

## 7.1 两个大阶段

文档处理不拆成过多 MQ 阶段，而是采用两个大阶段：

```text
DOCUMENT_PARSE_CHUNK
DOCUMENT_EMBEDDING_INDEX
```

Java system-service 控制大阶段流转；Python document-process-service 负责阶段内部细步骤。

### 7.1.1 DOCUMENT_PARSE_CHUNK

包含：

```text
文档下载
PDF 分析
Parser Router 路由
文档解析
结构识别
分片生成
解析产物归档 MinIO
分片元数据回调 Java
```

输出：

```text
sections.jsonl
document_structure.json
chunks.jsonl
chunk 元数据
```

### 7.1.2 DOCUMENT_EMBEDDING_INDEX

包含：

```text
读取 chunks.jsonl
调用 ai-model-service 生成 bge-m3 向量
chunk 正文写 Elasticsearch
chunk 向量写 Milvus
索引结果回调 Java
```

输出：

```text
ES index 信息
Milvus collection 信息
indexed_chunk_count
embedding_model_code
embedding_dimension
```

---

## 7.2 阶段状态

文档状态：

```text
UPLOADED
PARSE_CHUNKING
PARSE_CHUNKED
INDEXING
INDEXED
WAIT_AUDIT
AUDIT_PASSED
AUDIT_REJECTED
PUBLISHING
PUBLISHED
OFFLINING
OFFLINE
DELETED
PROCESS_FAILED
```

任务阶段状态：

```text
PENDING
QUEUED
RUNNING
SUCCESS
FAILED
RETRYING
CANCELED
```

Python 内部进度：

```text
PARSING
STRUCTURING
CHUNKING
EMBEDDING
ES_INDEXING
MILVUS_INDEXING
ARCHIVING
```

---

## 8. Java 与 Python 交互设计

## 8.1 文档处理任务

Java 发送 RabbitMQ 消息：

```json
{
  "messageId": "msg_20260624_000001",
  "taskId": "task_10001",
  "stageTaskId": "stage_20001",
  "stageCode": "DOCUMENT_PARSE_CHUNK",
  "knowledgeBaseId": 1,
  "directoryId": 10,
  "documentId": 1001,
  "versionId": 1,
  "processId": 90001,
  "fileResourceId": 30001,
  "fileName": "差旅报销制度.pdf",
  "fileType": "pdf",
  "bucketName": "rag-doc",
  "objectKey": "original/sha256/xxx.pdf",
  "chunkConfigSnapshot": {},
  "callbackUrl": "http://system-service/api/internal/document-process/callback",
  "retryCount": 0,
  "traceId": "trace_xxx",
  "createdTime": "2026-06-24 12:00:00"
}
```

## 8.2 Python 回调 Java

成功回调：

```json
{
  "taskId": "task_10001",
  "stageTaskId": "stage_20001",
  "stageCode": "DOCUMENT_PARSE_CHUNK",
  "status": "SUCCESS",
  "progress": 100,
  "result": {
    "sectionsObjectKey": "parsed/1/1001/1/90001/sections.jsonl",
    "structureObjectKey": "parsed/1/1001/1/90001/document_structure.json",
    "chunksObjectKey": "parsed/1/1001/1/90001/chunks.jsonl",
    "chunkCount": 96,
    "tokenCount": 68000
  },
  "traceId": "trace_xxx",
  "finishedTime": "2026-06-24 12:01:30"
}
```

失败回调：

```json
{
  "taskId": "task_10001",
  "stageTaskId": "stage_20001",
  "stageCode": "DOCUMENT_PARSE_CHUNK",
  "status": "FAILED",
  "progress": 35,
  "errorCode": "PDF_PARSE_ERROR",
  "errorMessage": "PDF 文件解析失败，可能为加密文件或损坏文件",
  "traceId": "trace_xxx",
  "finishedTime": "2026-06-24 12:01:30"
}
```

第一版内部回调暂不做复杂签名校验，但 Java 必须校验：

```text
taskId 是否存在
stageCode 是否匹配当前任务阶段
当前任务状态是否允许更新
回调状态是否合法
```

---

## 9. Parser Router 解析方案

## 9.1 设计原则

文档解析不能固定使用一个工具，需要采用 Parser Router 多解析器路由。

核心原则：

```text
文件后缀只作为初步判断
PDF 必须做内容形态分析
解析器可以多个
输出结构必须统一
后续分片不依赖具体解析器
```

## 9.2 PDF 识别流程

PDF 不直接按后缀解析，而是先做 `PdfAnalyzeService`。

分析指标：

```text
text_length
text_block_count
image_count
image_area_ratio
vector_draw_count
page_width
page_height
encrypted
page_count
table_heavy
image_heavy
```

页面类型：

```text
TEXT_PAGE
SCANNED_PAGE
MIXED_PAGE
TABLE_HEAVY_PAGE
IMAGE_HEAVY_PAGE
```

文件类型：

```text
TEXT_PDF
SCANNED_PDF
MIXED_PDF
TABLE_HEAVY_PDF
IMAGE_HEAVY_PDF
ENCRYPTED_PDF
DAMAGED_PDF
```

## 9.3 路由规则

| 类型 | 特征 | 解析方案 |
|---|---|---|
| 文本型 PDF | 可提取文本多，图片占比低 | PyMuPDF / pdfplumber / Docling |
| 扫描型 PDF | 文本少，大图覆盖整页 | visual-parser-service + MinerU |
| 混合型 PDF | 部分文本页，部分扫描页 | 页面级混合解析 |
| 表格密集 PDF | 表格多、线条多 | MinerU，后续 PP-Structure |
| 图片密集 PDF | 流程图、截图、图片多 | OCR，后续 VLM |
| Word | docx 文档 | python-docx / Docling 兜底 |
| Excel / CSV | 表格数据 | openpyxl / pandas / csv |
| Markdown | md 文档 | markdown-it-py |
| TXT | 纯文本 | 原生读取 |

## 9.4 扫描 PDF 判断

扫描页一般满足：

```text
text_length < 30
image_area_ratio >= 0.75
image_count >= 1
```

普通插图一般满足：

```text
text_length >= 100
image_area_ratio < 0.5
```

混合页一般满足：

```text
text_length >= 30
image_area_ratio >= 0.4
```

## 9.5 统一输出结构

所有解析器最终输出统一 DocumentStructure：

```json
{
  "document_id": "1001",
  "version_id": "1",
  "process_id": "90001",
  "parser_name": "mineru",
  "pages": [
    {
      "page_no": 1,
      "blocks": [
        {
          "block_id": "b001",
          "block_type": "title",
          "text": "第一章 总则",
          "html": null,
          "bbox": [0, 0, 100, 30],
          "confidence": 0.98,
          "source_parser": "mineru"
        }
      ]
    }
  ]
}
```

核心字段：

```text
block_id
block_type
text
html
page_no
sheet_name
row_start
row_end
bbox
title_path
confidence
source_parser
```

---

## 10. 分片策略设计

## 10.1 分片配置管理

分片配置由 Java system-service 管理，Python 按配置快照执行。

配置层级：

```text
系统默认配置
→ 知识库默认配置
→ 目录默认配置
→ 文档单独配置
```

优先级：

```text
文档配置 > 目录配置 > 知识库配置 > 系统配置
```

每次文档处理任务必须下发：

```text
chunk_config_snapshot
```

不能只传配置 ID，保证历史处理可追溯。

## 10.2 ChunkStrategy

不同文档类型使用不同策略：

```text
WordChunkStrategy
PdfChunkStrategy
ExcelChunkStrategy
MarkdownChunkStrategy
TextChunkStrategy
ImageChunkStrategy
```

规则：

```text
Word / Markdown：按标题层级 + 段落
PDF：按页码 + 标题识别 + 段落块 + 表格块
Excel / CSV：按 Sheet + 表头 + 行范围
扫描 PDF：按 OCR 块 + 版面区域 + 标题识别
图片 / 图表：按 OCR 文本 / 图片摘要 / VLM 摘要
```

## 10.3 Chunk 输出结构

```json
{
  "chunk_id": "doc_1001_ver_1_proc_90001_chunk_000001",
  "document_id": 1001,
  "version_id": 1,
  "process_id": 90001,
  "chunk_no": 1,
  "chunk_text": "员工出差住宿费按照城市等级标准报销……",
  "title_path": "报销制度 / 差旅报销 / 住宿标准",
  "source_type": "pdf_text",
  "page_start": 3,
  "page_end": 4,
  "sheet_name": null,
  "row_start": null,
  "row_end": null,
  "block_ids": ["b001", "b002"],
  "token_count": 512,
  "char_count": 860,
  "chunk_hash": "sha256_xxx"
}
```

## 10.4 Chunk ID 规则

Chunk ID 采用稳定业务 ID：

```text
chunk_id = doc_{documentId}_ver_{versionId}_proc_{processId}_chunk_{chunkNo}
```

示例：

```text
doc_1001_ver_3_proc_90001_chunk_000001
```

MySQL 同时保存：

```text
chunk_id
document_id
version_id
process_id
chunk_no
chunk_hash
es_doc_id
milvus_vector_id
```

---

## 11. 存储设计

## 11.1 存储分工

```text
MySQL：业务元数据、状态、配置、审核发布、任务记录
MinIO：原始文件、解析产物、分片产物、处理报告归档
Elasticsearch：chunk 正文、全文索引、分片预览、批量补全文本
Milvus：chunk 向量和向量检索
Redis：缓存、进度、锁、幂等、限流、会话临时上下文
```

### 11.1.1 MySQL 不存正文

MySQL 不存：

```text
原文正文
完整分片正文
大段表格正文
图片 OCR 正文
向量数组
```

MySQL 只存：

```text
object_key
es_index_name
es_doc_id
milvus_collection_name
milvus_vector_id
chunk_count
token_count
状态字段
统计字段
```

### 11.1.2 MinIO 归档路径

```text
original/{file_hash}/{file_name}
parsed/{knowledgeBaseId}/{documentId}/{versionId}/{processId}/sections.jsonl
parsed/{knowledgeBaseId}/{documentId}/{versionId}/{processId}/document_structure.json
parsed/{knowledgeBaseId}/{documentId}/{versionId}/{processId}/chunks.jsonl
parsed/{knowledgeBaseId}/{documentId}/{versionId}/{processId}/embeddings.jsonl
parsed/{knowledgeBaseId}/{documentId}/{versionId}/{processId}/process_report.json
visual/{knowledgeBaseId}/{documentId}/{versionId}/{processId}/mineru_result.json
```

### 11.1.3 JSONL 定位

JSONL 不作为在线查询主存储，只用于：

```text
解析产物归档
分片产物归档
问题排查
重建 ES 索引
重建 Milvus 索引
重新向量化
数据迁移
```

在线查询走 Elasticsearch。

---

## 12. ES 设计

## 12.1 Index 策略

第一版采用统一 Index：

```text
rag_chunk_index_v1
```

通过 metadata 过滤：

```text
knowledge_base_id
directory_id
document_id
version_id
process_id
publish_status
enabled
```

## 12.2 中文分词

Elasticsearch 8.x 使用 IK 分词器。

字段建议：

```text
chunk_text：ik_max_word
title_path：ik_max_word
document_name：ik_max_word
tags：keyword
chunk_id：keyword
knowledge_base_id：long
directory_id：long
document_id：long
version_id：long
process_id：long
publish_status：keyword
enabled：boolean
```

后续维护业务词典：

```text
报账
报销
差旅费
付款单
审批流
影像件
SAP
FICO
知识库
分片
召回
```

---

## 13. Milvus 设计

## 13.1 Collection 策略

同一 Embedding 模型共用一个 Collection。

第一版：

```text
rag_chunk_bge_m3_1024
```

后续如果切换模型，新建 Collection：

```text
rag_chunk_bge_small_512
rag_chunk_xxx_1536
```

## 13.2 字段设计

Milvus 存：

```text
chunk_id
knowledge_base_id
directory_id
document_id
version_id
process_id
embedding_vector
publish_status
enabled
created_time
```

不建议存完整 chunk 正文。

正式问答查询必须过滤：

```text
publish_status = PUBLISHED
enabled = true
```

---

## 14. 审核发布设计

## 14.1 写索引但默认禁用

文档处理完成后，先写 ES / Milvus，但默认：

```text
publish_status = UNPUBLISHED
enabled = false
```

这样管理端可以做发布前召回测试，但正式问答不会命中。

## 14.2 发布流程

```text
管理员点击发布
→ system-service 校验审核状态
→ MySQL 状态改为 PUBLISHING
→ system-service 发送 INDEX_STATUS_SYNC MQ
→ document-process-service 更新 ES / Milvus 状态
→ Python 回调 Java
→ Java 更新为 PUBLISHED
```

## 14.3 下架流程

```text
管理员点击下架
→ MySQL 状态改为 OFFLINING
→ system-service 发送 INDEX_STATUS_SYNC MQ
→ document-process-service 将 ES / Milvus enabled=false
→ Python 回调 Java
→ Java 更新为 OFFLINE
```

## 14.4 删除与恢复

删除使用逻辑删除。

规则：

```text
已发布文档不物理删除
删除 = 逻辑删除 + 索引禁用
删除后不能直接发布
必须先恢复
恢复后进入 OFFLINE / UNPUBLISHED
再重新发布
```

---

## 15. 文档版本设计

采用：

```text
document + document_version
```

规则：

```text
内容没变、处理产物没变，只改状态：不生成新版本
原文件、解析结果、分片结果、向量结果、索引结果任意变化：生成新版本
```

| 操作 | 是否新版本 |
|---|---|
| 发布 | 否 |
| 下架 | 否 |
| 重新发布 | 否 |
| 逻辑删除 | 否 |
| 删除后恢复 | 否 |
| 修改标题、标签、备注 | 否 |
| 重试失败阶段 | 否 |
| 重新上传文件 | 是 |
| 修改分片配置后重新处理 | 是 |
| 更换解析策略重新处理 | 是 |
| 更换 Embedding 模型重新向量化 | 是 |

同一文档同一时间只允许一个当前发布版本。

---

## 16. 文件去重与复用设计

## 16.1 file_hash 去重

上传时计算 file_hash。

规则：

```text
同一知识库 + 同一目录 + 相同 file_hash：提示重复，不默认上传
同一知识库 + 不同目录 + 相同 file_hash：允许复用，但提示已存在相同文件
同一文档重新上传相同 file_hash：不生成新版本，提示文件内容未变化
同一文档重新上传不同 file_hash：生成新的 document_version
```

## 16.2 文件资源复用

设计文件资源层：

```text
file_resource：按 file_hash 管理唯一原始文件
document：业务文档，挂知识库和目录
document_version：引用 file_resource
```

规则：

```text
相同 file_hash 的原始文件只存一份
不同目录 / 不同文档下的分片、索引、审核、发布状态独立生成和管理
```

---

## 17. 召回测试设计

召回测试入口在 Java system-service，执行在 Python ai-agent-service。

第一版流程：

```text
输入测试问题
→ system-service 接收测试请求
→ ai-agent-service 执行召回
→ ai-model-service 生成 query embedding
→ Milvus TopK 向量召回
→ Elasticsearch 批量补全 chunk 正文
→ 返回结果
```

支持测试范围：

```text
当前文档
当前目录
当前知识库
已发布内容
未发布内容
```

返回字段：

```text
排名
chunk_id
命中内容摘要
来源文档
来源目录
标题路径
页码 / Sheet / 行号
相似度
publish_status
enabled
```

第一版只启用向量召回。

预留但默认关闭：

```text
ES BM25
RRF 融合
Rerank
问题改写
```

---

## 18. Chat 服务扩展设计

## 18.1 chat-service 和 ai-agent-service 边界

```text
chat-service：会话主控
ai-agent-service：AI 编排
ai-model-service：模型调用
```

chat-service 负责：

```text
创建会话
保存消息
维护上下文
处理 SSE / WebSocket
调用 ai-agent-service
保存 AI 回复
处理转人工状态
```

ai-agent-service 负责：

```text
意图识别
问题改写
RAG 检索
上下文压缩
Prompt 组装
调用 ai-model-service
返回结构化结果
```

## 18.2 SSE 与 WebSocket

```text
SSE：知识库召回测试、普通模型问答、知识库问答流式输出
WebSocket：转人工、人工客服、实时双向会话、语音 / ASR 预留
```

---

## 19. 模型网关设计

## 19.1 多 Provider 配置

ai-model-service 不写死模型厂商，通过配置选择。

示例：

```yaml
ai:
  llm:
    default-provider: qwen
    default-model: qwen-plus
    providers:
      qwen:
        enabled: true
        base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
        api-key: ${QWEN_API_KEY}
        default-model: qwen-plus
      zhipu:
        enabled: false
        base-url: https://open.bigmodel.cn/api/paas/v4
        api-key: ${ZHIPU_API_KEY}
        default-model: glm-4-flash
      ollama:
        enabled: true
        base-url: http://localhost:11434
        default-model: qwen2.5:7b
      openai-compatible:
        enabled: false
        base-url: http://localhost:8000/v1
        api-key: ${OPENAI_COMPATIBLE_API_KEY}
```

## 19.2 场景模型配置

```yaml
ai:
  scene-model:
    chat:
      provider: qwen
      model: qwen-plus
    query-rewrite:
      provider: qwen
      model: qwen-turbo
    intent-detect:
      provider: ollama
      model: qwen2.5:7b
    embedding:
      provider: local
      model: bge-m3
    rerank:
      provider: local
      model: bge-reranker-v2-m3
```

## 19.3 Rerank

第一版预留但默认关闭。

```yaml
retrieval:
  rerank:
    enabled: false
    provider: local
    model: bge-reranker-v2-m3
    topNBeforeRerank: 30
    topNAfterRerank: 10
```

## 19.4 混合召回

第一版预留但默认关闭。

```yaml
retrieval:
  mode: vector
  vector:
    enabled: true
    topK: 10
  bm25:
    enabled: false
    topK: 20
  fusion:
    enabled: false
    type: rrf
```

---

## 20. 任务进度设计

后台文档处理进度采用：

```text
SSE 推送 + Redis 实时进度缓存 + MySQL 关键状态落库 + 查询接口兜底
```

流程：

```text
Python 回调 Java 进度
→ Java 更新 Redis 当前进度
→ Java 在关键节点更新 MySQL
→ Java 通过 SSE 推送给前端
```

Redis key：

```text
task:progress:{taskId}
```

示例：

```json
{
  "taskId": "10001",
  "documentId": "20001",
  "status": "RUNNING",
  "stage": "EMBEDDING",
  "progress": 72,
  "message": "正在生成分片向量",
  "updatedTime": "2026-06-24 15:20:00"
}
```

前端 SSE：

```text
GET /api/system/tasks/{taskId}/progress/stream
```

兜底查询：

```text
GET /api/system/tasks/{taskId}/progress
```

---

## 21. 异常重试设计

重试策略：

```text
自动重试 + 人工重试
自动重试最多 3 次
超过次数进入失败状态
后台人工处理
```

适合自动重试：

```text
MinIO 下载超时
ai-model-service 调用超时
ES 批量写入失败
Milvus 临时连接失败
RabbitMQ 消费异常
```

适合人工处理：

```text
文件损坏
加密 PDF
解析结果异常
分片效果不满意
Embedding 模型配置错误
ES / Milvus 索引结构错误
```

后台支持：

```text
重试当前大阶段
重新处理全文档
重新上传文件
查看失败日志
下载原始文件
删除文档
```

---

## 22. 日志与链路追踪

第一版采用统一 traceId + 日志上下文透传。

规则：

```text
HTTP 请求头：X-Trace-Id
RabbitMQ 消息体 / 消息头：traceId
Python 回调 Java：traceId
Java：MDC + Logback
Python：structlog / loguru
```

日志字段：

```text
traceId
userId
taskId
documentId
versionId
processId
stageCode
serviceName
```

第一版先不上 SkyWalking / OpenTelemetry，后续预留。

---

## 23. 监控告警设计

第一版采用轻量企业级方案。

包含：

```text
Java Spring Boot Actuator 健康检查
Python /health 健康检查
RabbitMQ 队列积压查看
死信队列查看
文档处理任务监控页面
任务成功数
任务失败数
平均耗时
失败阶段统计
最近异常记录
ES / Milvus / MinIO 连接检查
```

后续预留：

```text
Prometheus
Grafana
SkyWalking / OpenTelemetry
ELK / Loki
```

---

## 24. 接口文档设计

Java 服务：

```text
Knife4j + OpenAPI 3
```

Python 服务：

```text
FastAPI 自带 OpenAPI 文档
```

生产环境可关闭接口文档访问。

---

## 25. 数据库版本管理

使用 Flyway 管理数据库版本。

规则：

```text
Docker Compose：负责启动 MySQL 和创建基础数据库
Flyway：负责建表、字段变更、索引变更、初始化基础数据
```

目录：

```text
system-service/src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__init_system_data.sql
├── V3__add_document_process_tables.sql
├── V4__add_chat_tables.sql
```

---

## 26. 项目目录结构

```text
ai-knowledge-studio/
├── java/
│   ├── common/
│   │   ├── common-core/
│   │   ├── common-web/
│   │   ├── common-security/
│   │   ├── common-mybatis/
│   │   ├── common-redis/
│   │   ├── common-mq/
│   │   └── common-storage/
│   │
│   ├── gateway-service/
│   ├── system-service/
│   └── chat-service/
│
├── python/
│   ├── document-process-service/
│   ├── visual-parser-service/
│   ├── ai-model-service/
│   └── ai-agent-service/
│
├── frontend/
│   └── web/
│
├── deploy/
│   ├── docker-compose.yml
│   ├── nacos/
│   ├── mysql/
│   ├── redis/
│   ├── rabbitmq/
│   ├── minio/
│   ├── elasticsearch/
│   └── milvus/
│
└── docs/
    ├── 需求分析文档.md
    ├── 方案设计文档.md
    └── 接口说明.md
```

---

## 27. Docker Compose 本地开发环境

必须提供 `deploy/docker-compose.yml`，一键启动中间件。

包含：

```text
Nacos
MySQL 8
Redis
RabbitMQ
MinIO
Elasticsearch 8.x + IK
Milvus
```

Java / Python / 前端可本地 IDE 启动，后续再逐步容器化服务。

---

## 28. 开发阶段规划

### 阶段一：基础框架与中间件打通

目标：项目能启动，服务能互通。

内容：

```text
gateway-service
system-service
chat-service
Python 四个服务骨架
Nacos
MySQL
Redis
RabbitMQ
MinIO
ES 8.x + IK
Milvus
统一配置
统一日志 traceId
Docker Compose 本地环境
Flyway 初始化
Knife4j / OpenAPI
```

### 阶段二：知识入库核心闭环

目标：上传一个文档后，能生成分片并写入 ES / Milvus。

内容：

```text
知识库管理
目录管理
文档上传
file_hash 去重
MinIO 原始文件存储
RabbitMQ 文档处理任务
DOCUMENT_PARSE_CHUNK
Parser Router
visual-parser-service + MinerU
分片生成
DOCUMENT_EMBEDDING_INDEX
bge-m3 向量化
ES 写 chunk 正文
Milvus 写 chunk 向量
任务进度 SSE
分片预览
```

### 阶段三：审核发布与召回测试

目标：形成完整入库前治理闭环。

内容：

```text
审核通过 / 驳回
发布 / 下架
索引状态同步 MQ
管理端召回测试
Milvus TopK
ES 批量补全文本
未发布 / 已发布测试范围
测试记录
重新发布
逻辑删除和恢复
```

### 阶段四：chat-service 与 AI 问答扩展

目标：从治理能力扩展到用户侧聊天问答。

内容：

```text
普通单聊
知识库问答
SSE 流式输出
chat-service 调 ai-agent-service
ai-agent-service 调 ai-model-service
引用来源返回
WebSocket 转人工预留
问题改写预留
Rerank 预留
混合召回预留
```

---

## 29. 已确认关键决策清单

| 编号 | 决策项 | 结果 |
|---|---|---|
| 1 | 第一阶段范围 | 完整 RAG 入库闭环 |
| 2 | Java-Python 任务交互 | RabbitMQ 异步 |
| 3 | MQ | RabbitMQ |
| 4 | 文件存储 | MinIO |
| 5 | 数据库正文存储 | MySQL 只存元数据，不存正文 |
| 6 | 全文索引 | Elasticsearch 8.x |
| 7 | 向量库 | Milvus |
| 8 | Embedding | bge-m3 |
| 9 | 模型服务 | ai-model-service，多 Provider 可配置 |
| 10 | Java 服务 | gateway-service、system-service、chat-service、common |
| 11 | Python 服务 | document-process-service、visual-parser-service、ai-model-service、ai-agent-service |
| 12 | 文档处理阶段 | DOCUMENT_PARSE_CHUNK、DOCUMENT_EMBEDDING_INDEX |
| 13 | 索引写入方 | Python 直接写 MinIO / ES / Milvus |
| 14 | 解析方案 | Parser Router 多解析器路由 |
| 15 | 扫描 PDF | visual-parser-service + MinerU |
| 16 | 分片配置 | Java 管理配置，Python 按快照执行 |
| 17 | 分片策略 | 按文档类型使用不同 ChunkStrategy |
| 18 | 发布前索引 | 先写 ES/Milvus，默认 enabled=false |
| 19 | 发布状态同步 | Java 发 MQ，Python 更新 ES/Milvus |
| 20 | Redis | 第一版加入 |
| 21 | Java 技术栈 | JDK 21 + Spring Boot 3.x + Spring Cloud + MyBatis-Plus |
| 22 | Python 技术栈 | Python 3.11 + FastAPI + Pydantic v2 |
| 23 | 前端 | 一个 web 工程 |
| 24 | Nacos | Java 注册发现 + 配置中心，Python 不强制注册 |
| 25 | 流式通信 | SSE + WebSocket |
| 26 | 权限 | 第一版简单权限 |
| 27 | 登录 | 账号密码 + JWT，预留 SSO |
| 28 | 任务进度 | SSE + Redis + MySQL + 查询兜底 |
| 29 | 召回测试 | system-service 入口，ai-agent-service 执行 |
| 30 | 正式聊天 | chat-service 主控，ai-agent-service 编排 |
| 31 | LLM | 多 Provider 配置，不写死厂商 |
| 32 | Rerank | 预留，默认关闭 |
| 33 | 混合召回 | 预留，默认关闭 |
| 34 | 文档版本 | document + document_version |
| 35 | 文件去重 | file_hash 检测 |
| 36 | 文件复用 | 原始文件复用，分片和索引独立 |
| 37 | chunk_id | documentId + versionId + processId + chunkNo |
| 38 | Milvus Collection | 同模型共用 Collection |
| 39 | ES Index | 统一 Index + metadata 过滤 |
| 40 | ES 分词 | IK 分词器 |
| 41 | 开发流程 | 四阶段推进 |
| 42 | 本地环境 | Docker Compose 一键启动 |
| 43 | DB 版本 | Flyway |
| 44 | 接口文档 | Java Knife4j，Python FastAPI OpenAPI |
| 45 | 日志链路 | traceId + MDC / 日志上下文 |
| 46 | 监控 | 轻量健康检查 + 任务监控 |
| 47 | 重试 | 自动重试 3 次 + 人工重试 |
| 48 | 回调安全 | 第一版暂不复杂化，预留签名扩展 |

---

## 30. 后续扩展方向

后续可以扩展：

```text
复杂权限和数据权限
多租户
SSO / LDAP / 企业微信 / 钉钉登录
BM25 + 向量混合召回
RRF 融合
Rerank 重排
问题改写
查询扩展
上下文压缩
VLM 图片理解
表格结构增强
OCR 质量评估
分片质量评分
召回测试评估集
自动化回归测试
Agent 工具调用
转人工客服系统
语音 ASR
Prometheus / Grafana
SkyWalking / OpenTelemetry
K8s 部署
灰度发布
多模型成本统计
```

---

## 31. 结论

本方案采用 Java + Python 混合架构，Java 负责业务主控和平台治理，Python 负责文档处理、视觉解析、模型调用和 AI 编排。

第一阶段重点完成企业级 RAG 入库前治理闭环：

```text
文档上传
→ 文档解析
→ 结构识别
→ 分片生成
→ 向量化
→ ES / Milvus 入库
→ 召回测试
→ 审核发布
```

系统通过 RabbitMQ 解耦 Java 与 Python，通过 MinIO / ES / Milvus 分工存储，通过 Parser Router 解决不同文档类型解析问题，通过版本、任务、状态、发布、日志和进度设计保障可追溯和可运维。

该方案不是一次性堆功能，而是在企业级最终架构下，按阶段逐步实现核心能力，保证后续可以平滑扩展到正式知识问答、混合召回、Rerank、Agent、转人工和语音交互等场景。
