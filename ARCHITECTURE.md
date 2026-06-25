# 架构总览

本项目采用 Java + Python + 前端 + 中间件的组合架构。

## 服务划分

Java 侧：

- `gateway-service`：统一入口、路由、鉴权前置、限流、跨域、traceId 透传。
- `system-service`：后台系统和知识库治理主服务，负责用户权限、知识库、目录、文档、任务、分片配置、审核发布、召回测试入口。
- `chat-service`：聊天主控服务，负责会话、消息、知识问答入口和 SSE 输出。
- `common`：公共能力，不单独部署。

Python 侧：

- `document-process-service`：文档解析、分片、任务消费、结果回调。
- `visual-parser-service`：扫描 PDF、图片 PDF、视觉版面解析。
- `ai-model-service`：Embedding、Rerank、Completion 模型网关。
- `ai-agent-service`：检索编排、问答生成、引用来源组织。

## 主链路

文档入库链路：

```text
前端
→ gateway-service
→ system-service
→ MinIO 保存原始文件
→ MySQL 保存元数据和任务状态
→ RabbitMQ 投递处理任务
→ document-process-service 消费任务
→ visual-parser-service 处理视觉解析
→ ai-model-service 生成向量
→ document-process-service 写 ES / Milvus
→ document-process-service 回调 system-service
→ 前端查看进度和分片
```

召回测试链路：

```text
前端
→ gateway-service
→ system-service
→ ai-agent-service
→ ai-model-service / Milvus / Elasticsearch
→ system-service 返回召回测试结果
```

聊天问答链路：

```text
前端
→ gateway-service
→ chat-service
→ ai-agent-service
→ ai-model-service / Milvus / Elasticsearch
→ chat-service SSE 返回
```

## 数据边界

- MySQL：元数据、配置、任务状态、审核发布记录、操作日志。
- MinIO：原始文件和处理产物。
- Elasticsearch：分片正文、全文检索、预览。
- Milvus：向量索引。
- Redis：缓存、进度、幂等、锁、限流。
- RabbitMQ：Java 与 Python 的异步任务流转。
