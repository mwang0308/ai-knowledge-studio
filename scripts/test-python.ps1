$ErrorActionPreference = "Stop"

if (Test-Path -LiteralPath "python" -PathType Container) {
    if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
        Write-Host "未找到 python，跳过 Python 校验。"
        return
    }

    Write-Host "执行 Python 语法校验。"
    Get-ChildItem -LiteralPath "python" -Directory | ForEach-Object {
        Write-Host "校验 $($_.Name)。"
        python -m compileall -q $_.FullName
        if ($LASTEXITCODE -ne 0) {
            throw "Python 语法校验失败：$($_.Name)，退出码：$LASTEXITCODE"
        }
    }
} else {
    Write-Host "python 目录不存在，跳过 Python 测试。"
}
