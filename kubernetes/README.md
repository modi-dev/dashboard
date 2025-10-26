# ☸️ Kubernetes развертывание Server Dashboard

## 🎯 Обзор

Этот репозиторий содержит все необходимые файлы для развертывания Server Dashboard в Kubernetes кластере.

## 📁 Структура файлов

```
kubernetes/
├── setup-minikube.md          # Инструкции по настройке Minikube
├── backend-deployment.yaml    # Backend Deployment и Service
├── frontend-deployment.yaml   # Frontend Deployment и Service
├── postgres-deployment.yaml   # PostgreSQL Deployment, Service, PVC, Secret
├── ingress.yaml              # Ingress для доступа к приложению
├── deploy.sh                 # Скрипт развертывания
├── build-and-deploy.sh       # Скрипт сборки и развертывания
└── README.md                 # Этот файл
```

## 🚀 Быстрый старт

### **1. Установка инструментов**

#### **Автоматическая установка (рекомендуется):**
```powershell
# Запуск PowerShell от имени администратора
.\install-tools.ps1
```

#### **Ручная установка:**
```powershell
# kubectl
curl -LO "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"
Move-Item kubectl.exe C:\Windows\System32\

# Minikube
Invoke-WebRequest -Uri "https://storage.googleapis.com/minikube/releases/latest/minikube-windows-amd64.exe" -OutFile "minikube.exe"
Move-Item minikube.exe C:\Windows\System32\minikube.exe
```

#### **Docker Desktop:**
Скачайте с https://www.docker.com/products/docker-desktop

### **2. Запуск Minikube**
```bash
# Запуск кластера с Ingress
minikube start --driver=docker --addons=ingress

# Проверка статуса
minikube status
```

### **3. Быстрый старт (все в одном)**
```powershell
# Переход в папку kubernetes
cd kubernetes

# Автоматическая установка и развертывание
.\quick-start.ps1
```

### **4. Ручная сборка и развертывание**
```powershell
# Сборка образов и развертывание
.\build-and-deploy.sh
```

### **4. Доступ к приложению**
```bash
# Добавьте в /etc/hosts (Windows: C:\Windows\System32\drivers\etc\hosts)
127.0.0.1 server-dashboard.local

# Откройте в браузере
http://server-dashboard.local
```

## 🔧 Ручное развертывание

### **1. Создание namespace**
```bash
kubectl create namespace server-dashboard
```

### **2. Развертывание компонентов**
```bash
# PostgreSQL
kubectl apply -f postgres-deployment.yaml

# Backend
kubectl apply -f backend-deployment.yaml

# Frontend
kubectl apply -f frontend-deployment.yaml

# Ingress
kubectl apply -f ingress.yaml
```

### **3. Проверка статуса**
```bash
kubectl get all -n server-dashboard
```

## 📊 Мониторинг

### **Просмотр логов**
```bash
# Backend логи
kubectl logs -f deployment/server-dashboard-backend -n server-dashboard

# Frontend логи
kubectl logs -f deployment/server-dashboard-frontend -n server-dashboard

# PostgreSQL логи
kubectl logs -f deployment/postgres -n server-dashboard
```

### **Просмотр ресурсов**
```bash
# Все ресурсы
kubectl get all -n server-dashboard

# Подробная информация
kubectl describe pods -n server-dashboard

# Использование ресурсов
kubectl top pods -n server-dashboard
```

## 🛠️ Полезные команды

### **Minikube**
```bash
# Остановка кластера
minikube stop

# Удаление кластера
minikube delete

# Открытие Dashboard
minikube dashboard

# Просмотр сервисов
minikube service list
```

### **kubectl**
```bash
# Порт-форвардинг для тестирования
kubectl port-forward service/server-dashboard-frontend 8080:80 -n server-dashboard
kubectl port-forward service/server-dashboard-backend 3001:3001 -n server-dashboard

# Подключение к поду
kubectl exec -it <pod-name> -n server-dashboard -- /bin/bash

# Удаление ресурсов
kubectl delete -f backend-deployment.yaml
kubectl delete -f frontend-deployment.yaml
kubectl delete -f postgres-deployment.yaml
kubectl delete -f ingress.yaml
```

## 🔍 Troubleshooting

### **Проблемы с образами**
```bash
# Проверка образов в Minikube
minikube image ls

# Загрузка образа в Minikube
minikube image load server-dashboard-backend:latest
```

### **Проблемы с сетью**
```bash
# Проверка сервисов
kubectl get services -n server-dashboard

# Проверка Ingress
kubectl get ingress -n server-dashboard
```

### **Проблемы с базой данных**
```bash
# Проверка PostgreSQL
kubectl exec -it deployment/postgres -n server-dashboard -- psql -U postgres -d server_dashboard

# Проверка подключения
kubectl run postgres-client --rm -i --tty --restart=Never --image=postgres:15-alpine -- psql -h postgres-service -U postgres -d server_dashboard
```

## 📈 Масштабирование

### **Горизонтальное масштабирование**
```bash
# Увеличение количества реплик backend
kubectl scale deployment server-dashboard-backend --replicas=3 -n server-dashboard

# Увеличение количества реплик frontend
kubectl scale deployment server-dashboard-frontend --replicas=3 -n server-dashboard
```

### **Вертикальное масштабирование**
```bash
# Изменение ресурсов в deployment файлах
# Затем применить изменения
kubectl apply -f backend-deployment.yaml
```

## 🔒 Безопасность

### **Secrets**
```bash
# Создание нового пароля для PostgreSQL
echo -n "newpassword" | base64
# Обновите postgres-deployment.yaml с новым значением
```

### **Network Policies**
```bash
# Создание Network Policy для изоляции трафика
kubectl apply -f network-policy.yaml
```

## 📚 Дополнительные ресурсы

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Minikube Documentation](https://minikube.sigs.k8s.io/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [Nginx Ingress Controller](https://kubernetes.github.io/ingress-nginx/)

---

**Server Dashboard успешно развернут в Kubernetes!** 🎉
