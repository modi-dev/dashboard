# 🚀 Настройка Minikube для локального Kubernetes

## 📋 Предварительные требования

### **1. Установка Docker**
```bash
# Windows (через Chocolatey)
choco install docker-desktop

# Или скачать с официального сайта
# https://www.docker.com/products/docker-desktop
```

### **2. Установка Minikube**
```bash
# Windows (через Chocolatey)
choco install minikube

# Или через PowerShell
# Скачать с https://minikube.sigs.k8s.io/docs/start/
```

### **3. Установка kubectl**
```bash
# Windows (через Chocolatey)
choco install kubernetes-cli

# Или скачать с https://kubernetes.io/docs/tasks/tools/
```

## 🚀 Запуск Minikube

### **1. Запуск кластера**
```bash
# Запуск с Docker драйвером
minikube start --driver=docker

# Запуск с дополнительными ресурсами
minikube start --driver=docker --memory=4096 --cpus=2

# Запуск с включенным Ingress
minikube start --driver=docker --addons=ingress
```

### **2. Проверка статуса**
```bash
# Проверка статуса кластера
minikube status

# Проверка нод
kubectl get nodes

# Проверка всех ресурсов
kubectl get all
```

### **3. Включение аддонов**
```bash
# Включение Dashboard
minikube addons enable dashboard

# Включение Ingress
minikube addons enable ingress

# Включение Metrics Server
minikube addons enable metrics-server

# Проверка аддонов
minikube addons list
```

## 🔧 Настройка для нашего проекта

### **1. Создание namespace**
```bash
kubectl create namespace server-dashboard
```

### **2. Установка Helm (опционально)**
```bash
# Windows (через Chocolatey)
choco install kubernetes-helm

# Или через PowerShell
# Скачать с https://helm.sh/docs/intro/install/
```

## 📊 Полезные команды

### **Minikube команды:**
```bash
# Остановка кластера
minikube stop

# Удаление кластера
minikube delete

# Перезапуск кластера
minikube restart

# Просмотр логов
minikube logs

# Открытие Dashboard
minikube dashboard
```

### **kubectl команды:**
```bash
# Просмотр всех ресурсов
kubectl get all -n server-dashboard

# Просмотр подов
kubectl get pods -n server-dashboard

# Просмотр сервисов
kubectl get services -n server-dashboard

# Просмотр логов
kubectl logs -f deployment/server-dashboard-backend -n server-dashboard
```

## 🎯 Следующие шаги

1. **Создание Docker образов** для backend и frontend
2. **Написание Kubernetes манифестов** (Deployment, Service, ConfigMap)
3. **Настройка Ingress** для доступа к приложению
4. **Настройка мониторинга** с Prometheus и Grafana
5. **Настройка логирования** с ELK Stack

---

**Minikube готов к использованию!** 🎉

