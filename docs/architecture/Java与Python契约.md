# Java 与 Python 契约

## 任务投递

Java 通过 RabbitMQ 投递文档处理任务。

消息必须包含：

- `messageId`
- `taskId`
- `taskNo`
- `stageCode`
- `knowledgeBaseId`
- `directoryId`
- `documentId`
- `versionId`
- `fileResourceId`
- `fileName`
- `fileType`
- `bucketName`
- `objectKey`
- `chunkConfigSnapshot`
- `callbackUrl`

## 回调要求

Python 处理完成后回调 Java。

回调必须包含：

- `taskId`
- `documentId`
- `stageCode`
- `status`
- `progress`
- `result.sectionsObjectKey`
- `result.structureObjectKey`
- `result.chunksObjectKey`
- `result.chunkCount`
- `result.tokenCount`
- `errorCode`
- `errorMessage`

## 状态约定

- `PENDING`
- `PROCESSING`
- `SUCCESS`
- `FAILED`
- `CANCELED`

## ID 约定

- 文档入库治理链路中，前端、MQ、Python 回调、MinIO 解析产物路径、ES/Milvus 元数据过滤和对外查询参数使用业务 ID。
- 业务 ID 必须为 32 位无横杠 UUID，例如 `replace(uuid(), '-', '')` 或 `uuid.uuid4().hex`。
- `documentId`、`versionId`、`taskId`、`chunkId`、`sectionId`、`blockId` 等文档处理相关查询 ID 禁止使用数据库自增主键。
- MySQL 表可以保留自增 `id` 作为内部主键，但只允许在 Java Service/Mapper 内部关联使用，禁止返回给前端或投递给 Python。
- 分片产物中的 `block_id`、`section_id`、`chunk_id` 使用 UUID；展示名称使用 `block_name`、`title`、`title_path` 等字段承载。

## PDF 解析产物约定

- `chunkConfigSnapshot.parserType` 表示 PDF 解析器，允许值为 `docling`、`mineru`、`pdf`，缺省时按 `docling` 处理。
- Java 在创建文档版本时固化 `parserType`，Python 不允许根据运行环境自行改用其他解析器；所选解析器失败时任务应明确失败。
- 成功回调的 `result.parserName` 与 `document_structure.json.parser_name` 必须记录实际解析器类型。
- PDF 必须先按配置页数拆分解析块，再在每个解析块内部抽取页面元素。
- 默认解析块页数为 `DOCUMENT_PROCESS_PDF_PARSE_BLOCK_PAGES=2`。
- PDF 总页数小于等于配置值时，只生成一个全文解析块；大于配置值时，按页码范围生成多个解析块。
- 解析块用于表达页码范围和分片来源，目录结构必须由文本标题元素合并生成，不能用解析块替代目录节点。
- 页面元素分类包括标题、段落、表格、图片和页码。图片元素只作为元数据记录，禁止参与目录标题识别；未经过 OCR 或视觉模型生成语义描述的图片不能写入正文分片。
- `document_structure.json` 必须包含 `directory_tree`、`parse_blocks`、`sections`。
- `chunks.jsonl` 中每条分片必须尽量携带 `section_id`、`parse_block_id`、`parse_block_name`、`page_start`、`page_end`，便于前端按目录和解析块筛选。

## 约束

- 回调必须幂等。
- Java 以业务 `taskId` 做任务定位。
- Python 不直接修改 Java MySQL。
- Java 不直接依赖 Python 内部实现。
