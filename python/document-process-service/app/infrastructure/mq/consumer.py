from __future__ import annotations

import json
import logging
import threading
from datetime import datetime

import pika

from app.application.parse_chunk_pipeline import ParseChunkPipeline
from app.config import settings
from app.infrastructure.callback.system_service_client import SystemServiceCallbackClient
from app.schemas.callback import ProcessCallback
from app.schemas.task_message import DocumentProcessMessage

logger = logging.getLogger(__name__)


class DocumentProcessConsumer:
    """RabbitMQ 文档处理任务消费者。"""

    def __init__(self, pipeline: ParseChunkPipeline, callback_client: SystemServiceCallbackClient) -> None:
        self._pipeline = pipeline
        self._callback_client = callback_client
        self._thread: threading.Thread | None = None

    def start_background(self) -> None:
        if self._thread and self._thread.is_alive():
            return
        self._thread = threading.Thread(target=self._consume_forever, name="document-process-consumer", daemon=True)
        self._thread.start()

    def _consume_forever(self) -> None:
        credentials = pika.PlainCredentials(settings.rabbitmq_username, settings.rabbitmq_password)
        parameters = pika.ConnectionParameters(
            host=settings.rabbitmq_host,
            port=settings.rabbitmq_port,
            credentials=credentials,
            heartbeat=60,
            blocked_connection_timeout=30,
        )
        connection = pika.BlockingConnection(parameters)
        channel = connection.channel()
        channel.queue_declare(queue=settings.rabbitmq_queue, durable=True)
        channel.basic_qos(prefetch_count=1)
        channel.basic_consume(queue=settings.rabbitmq_queue, on_message_callback=self._handle_message)
        logger.info("文档处理 MQ 消费者已启动 queue=%s", settings.rabbitmq_queue)
        channel.start_consuming()

    def _handle_message(self, channel, method, properties, body: bytes) -> None:
        message: DocumentProcessMessage | None = None
        try:
            payload = json.loads(body.decode("utf-8"))
            message = DocumentProcessMessage.model_validate(payload)
            logger.info("消费文档处理消息 task_id=%s message_id=%s", message.taskId, message.messageId)
            self._post_running(message, 8)
            result = self._pipeline.run(message, lambda progress: self._post_running(message, progress))
            self._callback_client.post_callback(
                message.callbackUrl,
                ProcessCallback(
                    taskId=message.taskId,
                    documentId=message.documentId,
                    stageCode=message.stageCode,
                    status="SUCCESS",
                    progress=100,
                    result=result,
                    traceId=message.traceId,
                    finishedTime=datetime.now(),
                ),
            )
            channel.basic_ack(delivery_tag=method.delivery_tag)
        except Exception as exc:
            logger.exception("文档处理消息执行失败")
            if message is not None:
                self._post_failed(message, exc)
            channel.basic_ack(delivery_tag=method.delivery_tag)

    def _post_running(self, message: DocumentProcessMessage, progress: int) -> None:
        self._callback_client.post_callback(
            message.callbackUrl,
            ProcessCallback(
                taskId=message.taskId,
                documentId=message.documentId,
                stageCode=message.stageCode,
                status="RUNNING",
                progress=progress,
                message="文档解析与分片处理中",
                traceId=message.traceId,
            ),
        )

    def _post_failed(self, message: DocumentProcessMessage, exc: Exception) -> None:
        self._callback_client.post_callback(
            message.callbackUrl,
            ProcessCallback(
                taskId=message.taskId,
                documentId=message.documentId,
                stageCode=message.stageCode,
                status="FAILED",
                progress=30,
                errorCode=exc.__class__.__name__,
                errorMessage=str(exc)[:512],
                traceId=message.traceId,
                finishedTime=datetime.now(),
            ),
        )
