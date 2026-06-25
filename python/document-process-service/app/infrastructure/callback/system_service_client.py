import logging

import httpx

from app.schemas.callback import ProcessCallback

logger = logging.getLogger(__name__)


class SystemServiceCallbackClient:
    """system-service 回调客户端。"""

    def post_callback(self, callback_url: str, payload: ProcessCallback) -> None:
        logger.info(
            "post document process callback task_id=%s status=%s progress=%s",
            payload.taskId,
            payload.status,
            payload.progress,
        )
        response = httpx.post(
            callback_url,
            json=payload.model_dump(mode="json", exclude_none=True),
            timeout=15,
        )
        response.raise_for_status()
