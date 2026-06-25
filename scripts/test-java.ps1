$ErrorActionPreference = "Stop"

if (Test-Path -LiteralPath "java" -PathType Container) {
    Write-Host "执行 Java 测试。"
    # 后续接入：mvn test
} else {
    Write-Host "java 目录不存在，跳过 Java 测试。"
}
