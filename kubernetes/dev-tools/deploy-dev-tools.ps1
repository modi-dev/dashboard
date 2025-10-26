# PowerShell скрипт для развертывания Dev-Tools в Minikube

Write-Host "🛠️  Развертывание Dev-Tools в Minikube..." -ForegroundColor Green

# Проверка Minikube
Write-Host "☸️  Проверка Minikube..." -ForegroundColor Yellow
try {
    minikube status | Out-Null
    Write-Host "✅ Minikube запущен" -ForegroundColor Green
} catch {
    Write-Host "❌ Minikube не запущен. Запустите: minikube start --driver=docker --addons=ingress" -ForegroundColor Red
    exit 1
}

# Создание namespace
Write-Host "📁 Создание namespace dev-tools..." -ForegroundColor Yellow
kubectl create namespace dev-tools --dry-run=client -o yaml | kubectl apply -f -

# Развертывание Nginx
Write-Host "🌐 Развертывание Nginx..." -ForegroundColor Yellow
kubectl apply -f nginx-deployment.yaml

# Развертывание Redis
Write-Host "🔴 Развертывание Redis..." -ForegroundColor Yellow
kubectl apply -f redis-deployment.yaml

# Развертывание PostgreSQL
Write-Host "🐘 Развертывание PostgreSQL..." -ForegroundColor Yellow
kubectl apply -f postgres-dev.yaml

# Развертывание MongoDB
Write-Host "🍃 Развертывание MongoDB..." -ForegroundColor Yellow
kubectl apply -f mongodb-dev.yaml

# Развертывание Prometheus + Grafana
Write-Host "📊 Развертывание Prometheus + Grafana..." -ForegroundColor Yellow
kubectl apply -f grafana-prometheus.yaml

# Развертывание Elasticsearch + Kibana
Write-Host "🔍 Развертывание Elasticsearch + Kibana..." -ForegroundColor Yellow
kubectl apply -f elasticsearch-kibana.yaml

# Ожидание готовности
Write-Host "⏳ Ожидание готовности подов..." -ForegroundColor Yellow
Write-Host "Это может занять несколько минут..." -ForegroundColor Cyan

# Ожидание готовности основных сервисов
try {
    kubectl wait --for=condition=ready pod -l app=nginx-dev -n dev-tools --timeout=120s
    kubectl wait --for=condition=ready pod -l app=redis-dev -n dev-tools --timeout=120s
    kubectl wait --for=condition=ready pod -l app=postgres-dev -n dev-tools --timeout=120s
    kubectl wait --for=condition=ready pod -l app=mongodb-dev -n dev-tools --timeout=120s
    Write-Host "✅ Основные сервисы готовы" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Некоторые сервисы могут быть не готовы" -ForegroundColor Yellow
}

# Показ статуса
Write-Host "📊 Статус развертывания:" -ForegroundColor Cyan
kubectl get all -n dev-tools

# Инструкции по доступу
Write-Host "🌐 Доступ к сервисам:" -ForegroundColor Cyan
Write-Host "Добавьте в C:\Windows\System32\drivers\etc\hosts:" -ForegroundColor Yellow
Write-Host "127.0.0.1 nginx-dev.local" -ForegroundColor White
Write-Host "127.0.0.1 prometheus.local" -ForegroundColor White
Write-Host "127.0.0.1 grafana.local" -ForegroundColor White
Write-Host "127.0.0.1 elasticsearch.local" -ForegroundColor White
Write-Host "127.0.0.1 kibana.local" -ForegroundColor White

Write-Host "🔗 Ссылки для доступа:" -ForegroundColor Cyan
Write-Host "Nginx: http://nginx-dev.local" -ForegroundColor White
Write-Host "Prometheus: http://prometheus.local" -ForegroundColor White
Write-Host "Grafana: http://grafana.local (admin/admin)" -ForegroundColor White
Write-Host "Elasticsearch: http://elasticsearch.local" -ForegroundColor White
Write-Host "Kibana: http://kibana.local" -ForegroundColor White

Write-Host "🔧 Полезные команды:" -ForegroundColor Cyan
Write-Host "kubectl get all -n dev-tools" -ForegroundColor White
Write-Host "kubectl logs -f deployment/nginx-dev -n dev-tools" -ForegroundColor White
Write-Host "kubectl port-forward service/nginx-dev-service 8080:80 -n dev-tools" -ForegroundColor White

Write-Host "✅ Dev-Tools развернуты успешно!" -ForegroundColor Green

