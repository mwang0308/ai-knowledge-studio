# PDF 解析器配置与生产流水线重构

## 任务范围

- PDF 上传时在分片配置中提供 Docling、MinerU、PDF 原生三种解析器。
- 默认使用 Docling，并将选择固化到文档版本配置快照。
- Python 生产流水线按解析器类型分派，统一生成目录树、解析块和分片数据。

## 完成内容

- 前端仅在选择 PDF 后展示解析器列表，并将 `parserType` 随上传表单提交。
- Java 校验解析器类型，将其写入 `chunkConfigSnapshot`，重跑沿用版本快照。
- Python 默认读取 `docling`，实现 Docling、MinerU CLI、PDF 原生版面三条生产解析路径。
- 所有解析路径统一转换为页面元素和分页解析块，再生成 `document_structure.json`、`sections.jsonl`、`chunks.jsonl`。
- 回调 `parserName` 和结构产物 `parser_name` 记录实际解析器。

## 验证结果

- 前端 `npm run build` 通过。
- Java Maven 编译通过。
- Python PDF 流水线 8 个单元测试通过。
- 项目样例 PDF 使用生产 Docling 路径实测：2 个解析块、20 个目录节点、18 个分片。

## 页面与层级修正

- PDF 解析器配置移动到上传页右侧任务摘要区，采用紧凑的纵向选择卡片。
- Python 在下载、解析、分片、产物保存阶段上报真实进度；前端轮询并平滑展示阶段进度和错误重试状态。
- 文档预览使用可展开、可折叠的树控件；历史平铺数据在前端增加文档根节点，新解析数据保存真实父子 ID。
- Docling 标题读取使用 `SectionHeaderItem.level`，仅有二级标题时将首标题提升为文档根节点。
- 样例 PDF 复测结果：1 个根节点、19 个子节点，层级为 1 / 2 级。
