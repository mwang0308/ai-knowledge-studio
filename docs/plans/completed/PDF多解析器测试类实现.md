# PDF 多解析器测试类实现

## 目标

为项目内 PDF 文件增加可直接执行的测试类，覆盖默认 PDF 版面解析、MinerU 整文档解析和 Docling 整文档解析，并统一返回完整目录结构、分片数据和解析块信息。

## 实现内容

- 增加 `PdfParseTestRunner`，入参为项目内 PDF 路径，默认解析方式为 `pdf`。
- 支持 `pdf`、`mineru`、`docling` 三种解析方式。
- 三种方式均先确定分页解析块范围，再把解析元素归入对应解析块。
- 默认方式复用 `ParseChunkPipeline` 的 PyMuPDF/pdfplumber 元素提取逻辑。
- MinerU 方式调用整文档 CLI，优先读取 `content_list_v2.json`，兼容旧 `content_list.json`。
- Docling 方式调用 `DocumentConverter`，从元素 provenance 获取页码和位置。
- 外部解析结果统一转换为 `PdfElement`，继续复用局部目录生成、全局目录合并、分片和解析块摘要逻辑。
- 外部解析器提供的标题层级优先于字号样式启发式，保留 MinerU/Docling 的目录层级。
- 标题候选不再完全信任 `element_type`，会综合显式层级、字号、粗体、位置、编号模式和彩色背景二次判断。
- 深色背景上的白字不再作为不可见辅助层过滤，同时排除联系方式、表单键值等伪标题。
- 增加项目样例 PDF 的默认解析回归测试和命令行入口。

## 使用方式

```powershell
cd python/document-process-service
python -m tests.test_pdf_parse_pipeline "docs/AI应用开发(java方向)-王萌.pdf" --parser pdf
python -m tests.test_pdf_parse_pipeline "docs/AI应用开发(java方向)-王萌.pdf" --parser mineru --output tmp/mineru-result.json
python -m tests.test_pdf_parse_pipeline "docs/AI应用开发(java方向)-王萌.pdf" --parser docling --output tmp/docling-result.json
```

相对路径以项目根目录为基准。MinerU 和 Docling 属于可选重依赖，未安装时测试类会返回明确错误，不会静默切换解析器。

## 验证结果

- `python -m unittest discover -s tests -v`：6 个测试通过。
- `scripts/test-python.ps1`：4 个 Python 服务语法校验通过。
- 项目样例 PDF 共 4 页，默认按 2 页拆成 2 个解析块，生成 1 个根目录和 20 个分片。
- 已渲染检查样例 PDF 前两页，标题层级、正文和项目经历版式与解析目录一致。
- 当前环境未安装 MinerU 和 Docling，未执行两种重模型的真实推理；对应入口、错误分支和 MinerU v2 结构归一化已覆盖。
