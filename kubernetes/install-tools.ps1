# PowerShell скрипт для автоматической установки Kubernetes инструментов

Write-Host "🚀 Установка Kubernetes инструментов..." -ForegroundColor Green

# Проверка прав администратора
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "⚠️  Запустите PowerShell от имени администратора!" -ForegroundColor Red
    exit 1
}

# Создание временной папки
$tempDir = "$env:TEMP\k8s-tools"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

Write-Host "📦 Установка kubectl..." -ForegroundColor Yellow

# Установка kubectl
$kubectlVersion = "v1.28.0"
$kubectlUrl = "https://dl.k8s.io/release/$kubectlVersion/bin/windows/amd64/kubectl.exe"
$kubectlPath = "$tempDir\kubectl.exe"

try {
    Invoke-WebRequest -Uri $kubectlUrl -OutFile $kubectlPath -UseBasicParsing
    Copy-Item $kubectlPath "C:\Windows\System32\kubectl.exe" -Force
    Write-Host "✅ kubectl установлен успешно" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка установки kubectl: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "📦 Установка Minikube..." -ForegroundColor Yellow

# Установка Minikube
$minikubeUrl = "https://storage.googleapis.com/minikube/releases/latest/minikube-windows-amd64.exe"
$minikubePath = "$tempDir\minikube.exe"

try {
    Invoke-WebRequest -Uri $minikubeUrl -OutFile $minikubePath -UseBasicParsing
    Copy-Item $minikubePath "C:\Windows\System32\minikube.exe" -Force
    Write-Host "✅ Minikube установлен успешно" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка установки Minikube: $($_.Exception.Message)" -ForegroundColor Red
}

# Очистка временных файлов
Remove-Item -Path $tempDir -Recurse -Force

Write-Host "🔍 Проверка установки..." -ForegroundColor Yellow

# Проверка установки
try {
    $kubectlVersion = kubectl version --client --output=yaml | Select-String "gitVersion" | ForEach-Object { $_.Line.Split(':')[1].Trim() }
    Write-Host "✅ kubectl версия: $kubectlVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ kubectl не найден" -ForegroundColor Red
}

try {
    $minikubeVersion = minikube version --output=json | ConvertFrom-Json | Select-Object -ExpandProperty minikubeVersion
    Write-Host "✅ Minikube версия: $minikubeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Minikube не найден" -ForegroundColor Red
}

Write-Host "🐳 Проверка Docker..." -ForegroundColor Yellow

try {
    $dockerVersion = docker --version
    Write-Host "✅ Docker: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker не найден. Установите Docker Desktop с https://www.docker.com/products/docker-desktop" -ForegroundColor Red
}

Write-Host "🎯 Следующие шаги:" -ForegroundColor Cyan
Write-Host "1. Убедитесь, что Docker Desktop запущен" -ForegroundColor White
Write-Host "2. Запустите: minikube start --driver=docker --addons=ingress" -ForegroundColor White
Write-Host "3. Перейдите в папку kubernetes и запустите: .\deploy.ps1" -ForegroundColor White

Write-Host "✅ Установка завершена!" -ForegroundColor Green

