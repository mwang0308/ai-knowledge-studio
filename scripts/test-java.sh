#!/usr/bin/env bash
set -euo pipefail

if [ -d "java" ]; then
  echo "执行 Java 测试。"
  # 后续接入：mvn test
else
  echo "java 目录不存在，跳过 Java 测试。"
fi
