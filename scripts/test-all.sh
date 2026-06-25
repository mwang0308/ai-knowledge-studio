#!/usr/bin/env bash
set -euo pipefail

bash scripts/test-java.sh
bash scripts/test-python.sh
if [ -f "scripts/test-frontend.sh" ]; then
  bash scripts/test-frontend.sh
fi
echo "全部测试入口执行完成。"
