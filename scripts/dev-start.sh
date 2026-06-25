#!/usr/bin/env bash
set -euo pipefail

echo "本地开发启动入口："
echo "1. 启动中间件：docker compose -f deploy/docker-compose.yml up -d"
echo "2. 启动网关：cd java/ai-knowledge-studio-server && mvn -pl gateway-service spring-boot:run"
echo "3. 启动系统服务：cd java/ai-knowledge-studio-server && mvn -pl system-service spring-boot:run"
echo "4. 启动聊天服务：cd java/ai-knowledge-studio-server && mvn -pl chat-service spring-boot:run"
echo "5. 启动前端：cd frontend/web && npm run dev"
echo "6. 启动 Python 服务示例：cd python/document-process-service && python -m uvicorn app.main:app --reload --port 18081"
