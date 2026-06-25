#!/usr/bin/env bash
set -euo pipefail

bash scripts/test-java.sh
bash scripts/test-python.sh
echo "全部测试入口执行完成。"
