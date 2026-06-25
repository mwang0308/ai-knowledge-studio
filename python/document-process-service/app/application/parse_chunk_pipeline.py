from __future__ import annotations

import json
import logging
from dataclasses import dataclass

from app.config import settings
from app.infrastructure.storage.minio_client import MinioDocumentClient
from app.schemas.callback import CallbackResult
from app.schemas.task_message import DocumentProcessMessage

logger = logging.getLogger(__name__)


@dataclass
class Chunk:
    """轻量分片结果，后续可替换为正式 ChunkStrategy 输出。"""

    chunk_id: str
    chunk_no: int
    chunk_text: str
    token_count: int
    char_count: int


class ParseChunkPipeline:
    """文档解析与分片应用服务，负责生成归档产物摘要。"""

    def __init__(self, storage_client: MinioDocumentClient) -> None:
        self._storage_client = storage_client

    def run(self, message: DocumentProcessMessage) -> CallbackResult:
        logger.info(
            "parse chunk pipeline start task_id=%s document_id=%s version_id=%s object_key=%s",
            message.taskId,
            message.documentId,
            message.versionId,
            message.objectKey,
        )
        file_bytes = self._storage_client.download_bytes(message.bucketName, message.objectKey)
        text = self._extract_text(file_bytes, message.fileType, message.fileName)
        chunks = self._split_chunks(text, message)
        base_key = f"parsed/{message.knowledgeBaseId}/{message.documentId}/{message.versionId}/{message.taskId}"

        sections_key = f"{base_key}/sections.jsonl"
        structure_key = f"{base_key}/document_structure.json"
        chunks_key = f"{base_key}/chunks.jsonl"

        self._storage_client.upload_text(message.bucketName, sections_key, self._build_sections_jsonl(text), "application/x-jsonlines")
        self._storage_client.upload_text(message.bucketName, structure_key, self._build_structure_json(message, text), "application/json")
        self._storage_client.upload_text(message.bucketName, chunks_key, self._build_chunks_jsonl(message, chunks), "application/x-jsonlines")

        token_count = sum(chunk.token_count for chunk in chunks)
        logger.info(
            "parse chunk pipeline success task_id=%s chunk_count=%s token_count=%s",
            message.taskId,
            len(chunks),
            token_count,
        )
        return CallbackResult(
            sectionsObjectKey=sections_key,
            structureObjectKey=structure_key,
            chunksObjectKey=chunks_key,
            chunkCount=len(chunks),
            tokenCount=token_count,
            parserName="lightweight-parser",
        )

    def _extract_text(self, file_bytes: bytes, file_type: str, file_name: str) -> str:
        lower_type = file_type.lower()
        if lower_type in {"txt", "md", "csv"}:
            return file_bytes.decode("utf-8", errors="ignore").strip()
        # 第一版先保留非纯文本格式的处理入口，后续接 Docling/MinerU/office parser。
        return f"{file_name}\n当前文件类型为 {file_type}，已进入文档解析流程，正式解析器待接入。"

    def _split_chunks(self, text: str, message: DocumentProcessMessage) -> list[Chunk]:
        normalized = text.strip() or message.fileName
        max_chars = max(settings.chunk_max_chars, 200)
        chunks: list[Chunk] = []
        for start in range(0, len(normalized), max_chars):
            chunk_no = len(chunks) + 1
            chunk_text = normalized[start : start + max_chars]
            chunk_id = f"doc_{message.documentId}_ver_{message.versionId}_proc_{message.taskId}_chunk_{chunk_no:06d}"
            chunks.append(
                Chunk(
                    chunk_id=chunk_id,
                    chunk_no=chunk_no,
                    chunk_text=chunk_text,
                    token_count=max(1, len(chunk_text) // 2),
                    char_count=len(chunk_text),
                )
            )
        return chunks

    def _build_sections_jsonl(self, text: str) -> str:
        row = {"section_no": 1, "title_path": "root", "text_preview": text[:200]}
        return json.dumps(row, ensure_ascii=False) + "\n"

    def _build_structure_json(self, message: DocumentProcessMessage, text: str) -> str:
        structure = {
            "document_id": message.documentId,
            "version_id": message.versionId,
            "process_id": message.taskId,
            "parser_name": "lightweight-parser",
            "pages": [
                {
                    "page_no": 1,
                    "blocks": [
                        {
                            "block_id": "block_000001",
                            "block_type": "text",
                            "text": text[:500],
                            "source_parser": "lightweight-parser",
                        }
                    ],
                }
            ],
        }
        return json.dumps(structure, ensure_ascii=False, indent=2)

    def _build_chunks_jsonl(self, message: DocumentProcessMessage, chunks: list[Chunk]) -> str:
        rows = []
        for chunk in chunks:
            rows.append(
                json.dumps(
                    {
                        "chunk_id": chunk.chunk_id,
                        "document_id": message.documentId,
                        "version_id": message.versionId,
                        "process_id": message.taskId,
                        "chunk_no": chunk.chunk_no,
                        "chunk_text": chunk.chunk_text,
                        "title_path": "root",
                        "source_type": message.fileType,
                        "block_ids": ["block_000001"],
                        "token_count": chunk.token_count,
                        "char_count": chunk.char_count,
                    },
                    ensure_ascii=False,
                )
            )
        return "\n".join(rows) + "\n"
