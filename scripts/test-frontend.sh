#!/usr/bin/env bash
set -euo pipefail

if [ -d "frontend/web" ]; then
  if ! command -v npm >/dev/null 2>&1; then
    echo "未找到 npm，跳过前端校验。"
    exit 0
  fi

  echo "执行前端构建校验。"
  (
    cd frontend/web
    if [ ! -d "node_modules" ]; then
      npm install
    fi
    npm run build
  )
else
  echo "frontend/web 目录不存在，跳过前端校验。"
fi
