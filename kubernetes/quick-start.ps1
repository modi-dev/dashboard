# Быстрый старт для развертывания Server Dashboard

Write-Host "🚀 Быстрый старт Server Dashboard в Kubernetes" -ForegroundColor Green

# Проверка Docker
Write-Host "🐳 Проверка Docker..." -ForegroundColor Yellow
try {
    docker --version | Out-Null
    Write-Host "✅ Docker найден" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker не найден. Установите Docker Desktop!" -ForegroundColor Red
    Write-Host "Скачайте с: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# Проверка kubectl
Write-Host "🔧 Проверка kubectl..." -ForegroundColor Yellow
try {
    kubectl version --client | Out-Null
    Write-Host "✅ kubectl найден" -ForegroundColor Green
} catch {
    Write-Host "❌ kubectl не найден. Установите kubectl!" -ForegroundColor Red
    Write-Host "Запустите: .\install-tools.ps1" -ForegroundColor Yellow
    exit 1
}

# Проверка Minikube
Write-Host "☸️  Проверка Minikube..." -ForegroundColor Yellow
try {
    minikube version | Out-Null
    Write-Host "✅ Minikube найден" -ForegroundColor Green
} catch {
    Write-Host "❌ Minikube не найден. Установите Minikube!" -ForegroundColor Red
    Write-Host "Запустите: .\install-tools.ps1" -ForegroundColor Yellow
    exit 1
}

# Запуск Minikube
Write-Host "🚀 Запуск Minikube кластера..." -ForegroundColor Yellow
try {
    minikube start --driver=docker --addons=ingress --memory=4096 --cpus=2
    Write-Host "✅ Minikube запущен" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка запуска Minikube: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Создание namespace
Write-Host "📁 Создание namespace..." -ForegroundColor Yellow
kubectl create namespace server-dashboard --dry-run=client -o yaml | kubectl apply -f -

# Применение манифестов
Write-Host "📦 Развертывание PostgreSQL..." -ForegroundColor Yellow
kubectl apply -f postgres-deployment.yaml

Write-Host "📦 Развертывание Backend..." -ForegroundColor Yellow
kubectl apply -f backend-deployment.yaml

Write-Host "📦 Развертывание Frontend..." -ForegroundColor Yellow
kubectl apply -f frontend-deployment.yaml

Write-Host "🌐 Настройка Ingress..." -ForegroundColor Yellow
kubectl apply -f ingress.yaml

# Ожидание готовности
Write-Host "⏳ Ожидание готовности подов..." -ForegroundColor Yellow
Write-Host "Это может занять несколько минут..." -ForegroundColor Cyan

try {
    kubectl wait --for=condition=ready pod -l app=postgres -n server-dashboard --timeout=300s
    kubectl wait --for=condition=ready pod -l app=server-dashboard-backend -n server-dashboard --timeout=300s
    kubectl wait --for=condition=ready pod -l app=server-dashboard-frontend -n server-dashboard --timeout=300s
    Write-Host "✅ Все поды готовы" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Некоторые поды могут быть не готовы. Проверьте статус:" -ForegroundColor Yellow
    kubectl get pods -n server-dashboard
}

# Показ статуса
Write-Host "📊 Статус развертывания:" -ForegroundColor Cyan
kubectl get all -n server-dashboard

# Инструкции по доступу
Write-Host "🌐 Доступ к приложению:" -ForegroundColor Cyan
Write-Host "1. Добавьте в файл C:\Windows\System32\drivers\etc\hosts:" -ForegroundColor White
Write-Host "   127.0.0.1 server-dashboard.local" -ForegroundColor Yellow
Write-Host "2. Откройте в браузере: http://server-dashboard.local" -ForegroundColor White

Write-Host "🔧 Полезные команды:" -ForegroundColor Cyan
Write-Host "kubectl get all -n server-dashboard" -ForegroundColor White
Write-Host "kubectl logs -f deployment/server-dashboard-backend -n server-dashboard" -ForegroundColor White
Write-Host "minikube dashboard" -ForegroundColor White

Write-Host "✅ Развертывание завершено!" -ForegroundColor Green

