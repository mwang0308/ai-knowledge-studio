#!/usr/bin/env bash
set -euo pipefail

if [ -d "python" ]; then
  echo "执行 Python 测试。"
  # 后续接入：pytest
else
  echo "python 目录不存在，跳过 Python 测试。"
fi
