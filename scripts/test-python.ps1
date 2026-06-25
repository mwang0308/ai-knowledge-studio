$ErrorActionPreference = "Stop"

if (Test-Path -LiteralPath "python" -PathType Container) {
    Write-Host "执行 Python 测试。"
    # 后续接入：pytest
} else {
    Write-Host "python 目录不存在，跳过 Python 测试。"
}
