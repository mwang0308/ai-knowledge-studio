# MinerU 与 Docling 独立解析测试

## 目标

分别直接运行 MinerU 和 Docling 解析项目 PDF，保留厂商原始产物和厂商自身识别的标题，不经过项目的 `ParseChunkPipeline`、标题二次判断、目录合并或分片逻辑。

## 实现内容

- `tests/mineru_pdf_parser_test.py`：调用 MinerU CLI 解析整份 PDF。
- MinerU 原始输出完整保留，测试报告只汇总 MinerU `type=title` 元素。
- `tests/docling_pdf_parser_test.py`：调用 Docling `DocumentConverter` 解析整份 PDF。
- Docling 原始 JSON 和 Markdown 完整保留，测试报告只汇总 Docling `title/section_header` 元素。
- 两个测试入口相互独立，可以分别安装、分别执行、分别比较。
- 补充 `mineru[all]` 和 `docling` Python 依赖声明。

## 运行方式

```powershell
cd python/document-process-service

python -m tests.mineru_pdf_parser_test "docs/AI应用开发(java方向)-王萌.pdf"
python -m tests.docling_pdf_parser_test "docs/AI应用开发(java方向)-王萌.pdf"
```

默认结果目录：

- `output/parser-tests/mineru/<PDF文件名>/`
- `output/parser-tests/docling/<PDF文件名>/`

MinerU 可通过 `--backend`、`--method`、`--language` 切换官方解析参数；建议先比较 `--method auto` 与 `--method ocr`。
