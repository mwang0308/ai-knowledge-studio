$ErrorActionPreference = "Stop"

if (Test-Path -LiteralPath "java/ai-knowledge-studio-server" -PathType Container) {
    if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
        Write-Host "未找到 mvn，跳过 Java 构建。"
        return
    }

    Write-Host "执行 Java 构建校验。"
    Push-Location "java/ai-knowledge-studio-server"
    try {
        mvn test
        if ($LASTEXITCODE -ne 0) {
            throw "Java 构建校验失败，退出码：$LASTEXITCODE"
        }
    } finally {
        Pop-Location
    }
} else {
    Write-Host "java 目录不存在，跳过 Java 测试。"
}
