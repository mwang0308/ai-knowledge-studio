from __future__ import annotations

import json
import logging
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Any

from app.config import settings

logger = logging.getLogger(__name__)


def recognize_pdf_images(
    file_bytes: bytes,
    page_start: int,
    page_end: int,
    image_elements: list[dict[str, Any]],
) -> list[dict[str, Any]]:
    """调用 MinerU CLI 识别 PDF 指定页码范围内的图片/扫描内容。"""

    command_path = shutil.which(settings.mineru_command)
    if command_path is None:
        raise RuntimeError(
            f"未找到 MinerU 命令：{settings.mineru_command}。请先安装依赖："
            f"{sys.executable} -m pip install \"mineru[all]>=2.0.0\""
        )

    with tempfile.TemporaryDirectory(prefix="document-process-mineru-") as temp_dir:
        work_dir = Path(temp_dir)
        input_path = work_dir / "input.pdf"
        output_dir = work_dir / "output"
        input_path.write_bytes(file_bytes)

        command = [
            command_path,
            "-p",
            str(input_path),
            "-o",
            str(output_dir),
            "-b",
            settings.mineru_backend,
            "-m",
            settings.mineru_method,
            "-l",
            settings.mineru_lang,
            "-s",
            str(page_start - 1),
            "-e",
            str(page_end - 1),
        ]
        logger.info(
            "调用 MinerU OCR 开始 page_start=%s page_end=%s image_count=%s backend=%s method=%s",
            page_start,
            page_end,
            len(image_elements),
            settings.mineru_backend,
            settings.mineru_method,
        )
        completed = subprocess.run(
            command,
            capture_output=True,
            text=True,
            timeout=settings.mineru_timeout_seconds,
            check=False,
        )
        if completed.returncode != 0:
            message = (completed.stderr or completed.stdout or "").strip()
            raise RuntimeError(f"MinerU 执行失败 exit_code={completed.returncode} message={message[:500]}")

        elements = _read_mineru_content_list(output_dir, page_start, page_end)
        if not elements:
            elements = _read_mineru_markdown(output_dir, page_start)
        logger.info(
            "调用 MinerU OCR 完成 page_start=%s page_end=%s element_count=%s",
            page_start,
            page_end,
            len(elements),
        )
        return elements


def _read_mineru_content_list(output_dir: Path, page_start: int, page_end: int) -> list[dict[str, Any]]:
    content_files = sorted(output_dir.rglob("*_content_list.json"))
    if not content_files:
        return []

    elements: list[dict[str, Any]] = []
    for content_file in content_files:
        try:
            raw_items = json.loads(content_file.read_text(encoding="utf-8"))
        except Exception as exception:
            logger.warning("读取 MinerU content_list 失败 file=%s error=%s", content_file.name, exception)
            continue
        if not isinstance(raw_items, list):
            continue
        for item in raw_items:
            element = _content_item_to_element(item, page_start, page_end)
            if element is not None:
                elements.append(element)
    return elements


def _content_item_to_element(
    item: Any,
    page_start: int,
    page_end: int,
) -> dict[str, Any] | None:
    if not isinstance(item, dict):
        return None

    page_no = _content_item_page_no(item, page_start, page_end)
    if page_no < page_start or page_no > page_end:
        return None

    item_type = str(item.get("type") or item.get("element_type") or "").lower()
    text = _content_item_text(item)
    if not text:
        return None

    element_type = _content_item_type(item_type, text)
    bbox = item.get("bbox") if isinstance(item.get("bbox"), list) else None
    return {
        "page_no": page_no,
        "element_type": element_type,
        "text": text,
        "bbox": bbox[:4] if bbox and len(bbox) >= 4 else [0.0, 0.0, 0.0, 0.0],
        "source_type": item_type,
    }


def _content_item_page_no(item: dict[str, Any], page_start: int, page_end: int) -> int:
    raw_page = item.get("page_idx")
    if raw_page is None:
        raw_page = item.get("page_no") or item.get("pageNo")
    try:
        page_value = int(raw_page)
    except (TypeError, ValueError):
        return page_start

    absolute_zero_based = page_value + 1
    if page_start <= absolute_zero_based <= page_end:
        return absolute_zero_based
    relative_zero_based = page_start + page_value
    if page_start <= relative_zero_based <= page_end:
        return relative_zero_based
    if page_start <= page_value <= page_end:
        return page_value
    return page_start


def _content_item_text(item: dict[str, Any]) -> str:
    for key in ("text", "table_body", "table_caption", "img_caption", "equation"):
        value = item.get(key)
        if isinstance(value, str) and value.strip():
            return value.strip()
        if isinstance(value, list):
            text = "\n".join(str(part).strip() for part in value if str(part).strip()).strip()
            if text:
                return text
    return ""


def _content_item_type(item_type: str, text: str) -> str:
    if item_type in {"title"}:
        return "title"
    if item_type in {"table"}:
        return "table"
    if item_type in {"page_number", "page-footer", "page_header", "page_footer"}:
        return "page_number"
    if len(text) <= 80 and item_type in {"heading", "header"}:
        return "title"
    return "paragraph"


def _read_mineru_markdown(output_dir: Path, page_start: int) -> list[dict[str, Any]]:
    markdown_files = sorted(output_dir.rglob("*.md"))
    elements: list[dict[str, Any]] = []
    for markdown_file in markdown_files:
        text = markdown_file.read_text(encoding="utf-8", errors="ignore").strip()
        if not text:
            continue
        elements.append(
            {
                "page_no": page_start,
                "element_type": "paragraph",
                "text": text,
                "bbox": [0.0, 0.0, 0.0, 0.0],
                "source_type": "markdown",
            }
        )
    return elements
