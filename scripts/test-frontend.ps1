$ErrorActionPreference = "Stop"

if (Test-Path -LiteralPath "frontend/web" -PathType Container) {
    if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
        Write-Host "未找到 npm，跳过前端校验。"
        return
    }

    Write-Host "执行前端构建校验。"
    Push-Location "frontend/web"
    try {
        if (-not (Test-Path -LiteralPath "node_modules" -PathType Container)) {
            npm install
            if ($LASTEXITCODE -ne 0) {
                throw "前端依赖安装失败，退出码：$LASTEXITCODE"
            }
        }

        npm run build
        if ($LASTEXITCODE -ne 0) {
            throw "前端构建校验失败，退出码：$LASTEXITCODE"
        }
    } finally {
        Pop-Location
    }
} else {
    Write-Host "frontend/web 目录不存在，跳过前端校验。"
}
