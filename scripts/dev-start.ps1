$ErrorActionPreference = "Stop"

Write-Host "本地开发启动入口："
Write-Host "1. 启动中间件：docker compose -f deploy/docker-compose.yml up -d"
Write-Host "2. 启动网关：cd java/ai-knowledge-studio-server && mvn -pl gateway-service spring-boot:run"
Write-Host "3. 启动系统服务：cd java/ai-knowledge-studio-server && mvn -pl system-service spring-boot:run"
Write-Host "4. 启动聊天服务：cd java/ai-knowledge-studio-server && mvn -pl chat-service spring-boot:run"
Write-Host "5. 启动前端：cd frontend/web && npm run dev"
Write-Host "6. 启动 Python 服务示例：cd python/document-process-service && python -m uvicorn app.main:app --reload --port 18081"
