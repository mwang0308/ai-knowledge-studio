$ErrorActionPreference = "Stop"

& "$PSScriptRoot/test-java.ps1"
& "$PSScriptRoot/test-python.ps1"
if (Test-Path -LiteralPath "$PSScriptRoot/test-frontend.ps1") {
    & "$PSScriptRoot/test-frontend.ps1"
}
Write-Host "全部测试入口执行完成。"
