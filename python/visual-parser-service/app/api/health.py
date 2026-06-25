from fastapi import APIRouter

from app.config import settings

router = APIRouter(tags=["health"])


@router.get("/health")
def health() -> dict[str, str]:
    """服务健康检查接口，不承载业务逻辑。"""
    return {"status": "UP", "service": settings.service_name}
