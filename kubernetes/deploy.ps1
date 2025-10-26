# PowerShell скрипт для развертывания Server Dashboard в Kubernetes

Write-Host "🚀 Развертывание Server Dashboard в Kubernetes..." -ForegroundColor Green

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

# Ожидание готовности подов
Write-Host "⏳ Ожидание готовности подов..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=postgres -n server-dashboard --timeout=300s
kubectl wait --for=condition=ready pod -l app=server-dashboard-backend -n server-dashboard --timeout=300s
kubectl wait --for=condition=ready pod -l app=server-dashboard-frontend -n server-dashboard --timeout=300s

Write-Host "✅ Развертывание завершено!" -ForegroundColor Green

# Показ статуса
Write-Host "📊 Статус развертывания:" -ForegroundColor Cyan
kubectl get all -n server-dashboard

Write-Host "🌐 Доступ к приложению:" -ForegroundColor Cyan
Write-Host "Frontend: http://server-dashboard.local" -ForegroundColor White
Write-Host "Backend API: http://server-dashboard.local/api" -ForegroundColor White

Write-Host "📝 Для доступа добавьте в C:\Windows\System32\drivers\etc\hosts:" -ForegroundColor Yellow
Write-Host "127.0.0.1 server-dashboard.local" -ForegroundColor White

