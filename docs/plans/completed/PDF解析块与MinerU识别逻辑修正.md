# PDF 解析块与 MinerU 识别逻辑修正

## 背景

PDF 解析逻辑需要严格按以下顺序执行：

```text
按 PDF 页码数拆解析块
-> 每个解析块解析文字、图片、表格
-> 图片元素接入 MinerU 识别
-> 每个解析块识别标题并生成局部目录
-> 合并所有解析块目录
-> 输出整篇文档结构目录到 MinIO，并由 Java 回调落库
```

## 完成内容

- PDF 继续按 `DOCUMENT_PROCESS_PDF_PARSE_BLOCK_PAGES` 拆解析块，默认 2 页一块。
- 每个 PDF 解析块内部固定抽取三类核心元素：文本、表格、图片。
- 图片元素不再只做占位；只要解析块内存在图片元素，就进入 MinerU 识别入口。
- MinerU 适配器入参包含原始 PDF 字节、页码范围和图片元素 bbox/元数据。
- MinerU 输出会规范化为 PDF 元素，支持 `title`、`paragraph`、`table`、`page_number`。
- MinerU 识别出的标题参与目录识别；识别出的正文/表格可以进入后续分片。
- 每个解析块生成 `local_directory_tree`，写入解析块 metadata。
- 所有解析块标题继续合并生成整篇 `directory_tree`，并写入 `document_structure.json`。
- `document_structure.json` 仍包含 `directory_tree`、`parse_blocks`、`sections`，保持 Java 回调落库链路不变。

## 配置

- `DOCUMENT_PROCESS_PDF_PARSE_BLOCK_PAGES`：PDF 每个解析块页数，默认 `2`。
- `DOCUMENT_PROCESS_ENABLE_MINERU_OCR`：是否启用 MinerU 图片识别，默认 `true`。
- `DOCUMENT_PROCESS_MINERU_OCR_ADAPTER`：MinerU 适配器函数，格式为 `module:function`，默认使用 `app.infrastructure.ocr.mineru_adapter:recognize_pdf_images`。

## 验证

- 已执行 `python -m compileall app`。
- 已执行内存级 PDF 校验：3 页 PDF 默认拆成 2 个解析块。
- 校验中注入 fake MinerU 适配器，带图片解析块的 `ocr_status=success`。
- MinerU 识别出的图片说明文字进入 chunk。
- 解析块局部目录和整篇 `directory_tree` 均生成成功。
