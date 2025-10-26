#!/bin/bash

# Скрипт для сборки Docker образов и развертывания

echo "🐳 Сборка Docker образов..."

# Сборка backend образа
echo "📦 Сборка backend образа..."
cd ../backend
docker build -t server-dashboard-backend:latest .
cd ../kubernetes

# Сборка frontend образа
echo "📦 Сборка frontend образа..."
cd ../client
docker build -t server-dashboard-frontend:latest .
cd ../kubernetes

# Загрузка образов в Minikube
echo "📤 Загрузка образов в Minikube..."
minikube image load server-dashboard-backend:latest
minikube image load server-dashboard-frontend:latest

# Развертывание
echo "🚀 Развертывание в Kubernetes..."
./deploy.sh

echo "✅ Сборка и развертывание завершены!"

