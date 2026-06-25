#!/usr/bin/env bash
set -euo pipefail

if [ -d "java/ai-knowledge-studio-server" ]; then
  if ! command -v mvn >/dev/null 2>&1; then
    echo "未找到 mvn，跳过 Java 构建。"
    exit 0
  fi

  echo "执行 Java 构建校验。"
  (cd java/ai-knowledge-studio-server && mvn test)
else
  echo "java 目录不存在，跳过 Java 测试。"
fi
