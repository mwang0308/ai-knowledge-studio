#!/usr/bin/env bash
set -euo pipefail

if [ -d "python" ]; then
  if ! command -v python >/dev/null 2>&1; then
    echo "未找到 python，跳过 Python 校验。"
    exit 0
  fi

  echo "执行 Python 语法校验。"
  for service_dir in python/*; do
    if [ -d "$service_dir" ]; then
      echo "校验 $(basename "$service_dir")。"
      python -m compileall -q "$service_dir"
    fi
  done
else
  echo "python 目录不存在，跳过 Python 测试。"
fi
