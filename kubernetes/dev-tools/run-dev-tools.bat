@echo off
echo 🛠️  Развертывание Dev-Tools в Minikube...

REM Проверка Minikube
echo ☸️  Проверка Minikube...
minikube status >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Minikube не запущен. Запустите: minikube start --driver=docker --addons=ingress
    pause
    exit /b 1
)
echo ✅ Minikube запущен

REM Создание namespace
echo 📁 Создание namespace dev-tools...
kubectl create namespace dev-tools --dry-run=client -o yaml | kubectl apply -f -

REM Развертывание сервисов
echo 🌐 Развертывание Nginx...
kubectl apply -f nginx-deployment.yaml

echo 🔴 Развертывание Redis...
kubectl apply -f redis-deployment.yaml

echo 🐘 Развертывание PostgreSQL...
kubectl apply -f postgres-dev.yaml

echo 🍃 Развертывание MongoDB...
kubectl apply -f mongodb-dev.yaml

echo 📊 Развертывание Prometheus + Grafana...
kubectl apply -f grafana-prometheus.yaml

echo 🔍 Развертывание Elasticsearch + Kibana...
kubectl apply -f elasticsearch-kibana.yaml

echo ⏳ Ожидание готовности подов...
echo Это может занять несколько минут...

REM Ожидание готовности основных сервисов
kubectl wait --for=condition=ready pod -l app=nginx-dev -n dev-tools --timeout=120s
kubectl wait --for=condition=ready pod -l app=redis-dev -n dev-tools --timeout=120s
kubectl wait --for=condition=ready pod -l app=postgres-dev -n dev-tools --timeout=120s
kubectl wait --for=condition=ready pod -l app=mongodb-dev -n dev-tools --timeout=120s

echo ✅ Основные сервисы готовы

echo 📊 Статус развертывания:
kubectl get all -n dev-tools

echo.
echo 🌐 Доступ к сервисам:
echo Добавьте в C:\Windows\System32\drivers\etc\hosts:
echo 127.0.0.1 nginx-dev.local
echo 127.0.0.1 prometheus.local
echo 127.0.0.1 grafana.local
echo 127.0.0.1 elasticsearch.local
echo 127.0.0.1 kibana.local

echo.
echo 🔗 Ссылки для доступа:
echo Nginx: http://nginx-dev.local
echo Prometheus: http://prometheus.local
echo Grafana: http://grafana.local (admin/admin)
echo Elasticsearch: http://elasticsearch.local
echo Kibana: http://kibana.local

echo.
echo ✅ Dev-Tools развернуты успешно!
pause

