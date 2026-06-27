# Python 编码规范

## 适用范围

适用于文档解析、视觉解析、模型网关、Agent 编排等 Python 服务。

## 命名

- 文件名使用小写下划线。
- 类名使用 UpperCamelCase。
- 函数和变量使用 lower_snake_case。

## 接口

- FastAPI 接口必须定义 Pydantic 模型。
- 入参和出参不能直接使用 dict 透传。

## 日志

- 任务开始、任务结束、失败重试、外部服务调用必须记录日志。
- 日志必须包含 `task_id`、`document_id`、`stage`。
- 业务日志使用中文描述，保留 `task_id`、`document_id`、`stage` 等字段名便于检索。
- 不记录文件正文和敏感数据。

## ID 规范

- Python 接收和回调的 `taskId`、`documentId`、`versionId` 使用字符串类型，值为 32 位无横杠 UUID。
- Python 生成的 `chunk_id`、`section_id`、`block_id` 必须使用 `uuid.uuid4().hex`。
- 解析块展示名称、章节标题和标题路径不能复用 ID 字段，应分别写入 `block_name`、`title`、`title_path`。

## PDF 解析规范

- PDF 默认使用 Docling，按 `chunkConfigSnapshot.parserType` 分派到 Docling、MinerU 或原生 PDF 解析器。
- 禁止解析器静默降级；选择的解析器不可用或失败时必须让任务失败并回调明确原因。
- PDF 文件必须先按配置页码范围拆出解析块，再对每个解析块单独抽取页面元素。
- 默认拆解析块页数为 2，可通过 `DOCUMENT_PROCESS_PDF_PARSE_BLOCK_PAGES` 覆盖。
- 页面元素必须先分类为标题、段落、表格、图片、页码，再参与目录和正文生成。
- 目录树只能从文本标题元素合并生成，图片元素、页码元素和纯装饰符号不能参与目录识别。
- 分片必须来源于解析块内的正文和表格文本，并携带解析块 ID、解析块名称、页码范围和目录节点 ID。
- 未经过 OCR 或视觉模型生成语义描述的图片元素不能写入正文分片；装饰性背景条、分割线、页眉页脚图片必须过滤。

## 异常

- 可恢复异常要返回明确错误码。
- 不吞异常。
- 回调 Java 时必须带上失败原因。
