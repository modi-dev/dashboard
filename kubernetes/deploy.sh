#!/bin/bash

# Скрипт для развертывания Server Dashboard в Kubernetes

echo "🚀 Развертывание Server Dashboard в Kubernetes..."

# Создание namespace
echo "📁 Создание namespace..."
kubectl create namespace server-dashboard --dry-run=client -o yaml | kubectl apply -f -

# Применение манифестов
echo "📦 Развертывание PostgreSQL..."
kubectl apply -f postgres-deployment.yaml

echo "📦 Развертывание Backend..."
kubectl apply -f backend-deployment.yaml

echo "📦 Развертывание Frontend..."
kubectl apply -f frontend-deployment.yaml

echo "🌐 Настройка Ingress..."
kubectl apply -f ingress.yaml

# Ожидание готовности подов
echo "⏳ Ожидание готовности подов..."
kubectl wait --for=condition=ready pod -l app=postgres -n server-dashboard --timeout=300s
kubectl wait --for=condition=ready pod -l app=server-dashboard-backend -n server-dashboard --timeout=300s
kubectl wait --for=condition=ready pod -l app=server-dashboard-frontend -n server-dashboard --timeout=300s

echo "✅ Развертывание завершено!"

# Показ статуса
echo "📊 Статус развертывания:"
kubectl get all -n server-dashboard

echo "🌐 Доступ к приложению:"
echo "Frontend: http://server-dashboard.local"
echo "Backend API: http://server-dashboard.local/api"

echo "📝 Для доступа добавьте в /etc/hosts:"
echo "127.0.0.1 server-dashboard.local"

