# 🚀 Установка Kubernetes инструментов без Chocolatey

## 📋 Установка kubectl

### **Windows (PowerShell):**

#### **Способ 1: Через curl**
```powershell
# Скачивание kubectl
curl -LO "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"

# Перемещение в PATH
Move-Item kubectl.exe C:\Windows\System32\

# Проверка установки
kubectl version --client
```

#### **Способ 2: Через PowerShell (автоматический)**
```powershell
# Скачивание и установка kubectl
$kubectlVersion = "v1.28.0"
$kubectlUrl = "https://dl.k8s.io/release/$kubectlVersion/bin/windows/amd64/kubectl.exe"
$kubectlPath = "$env:USERPROFILE\kubectl.exe"

Invoke-WebRequest -Uri $kubectlUrl -OutFile $kubectlPath
Move-Item $kubectlPath C:\Windows\System32\kubectl.exe

# Проверка
kubectl version --client
```

## 📋 Установка Minikube

### **Windows (PowerShell):**

#### **Способ 1: Прямое скачивание**
```powershell
# Скачивание Minikube
$minikubeUrl = "https://storage.googleapis.com/minikube/releases/latest/minikube-windows-amd64.exe"
$minikubePath = "$env:USERPROFILE\minikube.exe"

Invoke-WebRequest -Uri $minikubeUrl -OutFile $minikubePath
Move-Item $minikubePath C:\Windows\System32\minikube.exe

# Проверка
minikube version
```

#### **Способ 2: Через winget (если доступен)**
```powershell
# Установка через winget
winget install Kubernetes.minikube
winget install Kubernetes.kubectl
```

## 📋 Установка Docker Desktop

### **Скачивание с официального сайта:**
1. Перейдите на https://www.docker.com/products/docker-desktop
2. Скачайте Docker Desktop для Windows
3. Установите и запустите Docker Desktop
4. Убедитесь, что Docker запущен: `docker --version`

## 🚀 Запуск Minikube

### **1. Запуск кластера:**
```powershell
# Запуск с Docker драйвером
minikube start --driver=docker

# Запуск с дополнительными ресурсами
minikube start --driver=docker --memory=4096 --cpus=2

# Запуск с включенным Ingress
minikube start --driver=docker --addons=ingress
```

### **2. Проверка статуса:**
```powershell
# Статус кластера
minikube status

# Проверка нод
kubectl get nodes

# Проверка всех ресурсов
kubectl get all
```

## 🔧 Альтернативные способы установки

### **1. Через Scoop (если установлен):**
```powershell
# Установка Scoop (если не установлен)
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
irm get.scoop.sh | iex

# Установка через Scoop
scoop install kubectl
scoop install minikube
```

### **2. Через winget:**
```powershell
# Проверка доступности winget
winget --version

# Установка (если winget доступен)
winget install Kubernetes.minikube
winget install Kubernetes.kubectl
```

### **3. Ручная установка:**
1. **kubectl:** https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/
2. **Minikube:** https://minikube.sigs.k8s.io/docs/start/
3. **Docker:** https://docs.docker.com/desktop/install/windows-install/

## 🎯 Проверка установки

### **Проверка всех компонентов:**
```powershell
# Проверка Docker
docker --version

# Проверка kubectl
kubectl version --client

# Проверка Minikube
minikube version

# Запуск кластера
minikube start --driver=docker

# Проверка кластера
kubectl get nodes
```

## 🚀 Быстрый старт после установки

### **1. Запуск Minikube:**
```powershell
minikube start --driver=docker --addons=ingress
```

### **2. Переход в папку kubernetes:**
```powershell
cd kubernetes
```

### **3. Развертывание приложения:**
```powershell
# Windows PowerShell
.\deploy.ps1

# Или Linux/Mac
./deploy.sh
```

## 🔍 Troubleshooting

### **Проблемы с правами:**
```powershell
# Запуск PowerShell от имени администратора
# Или установка в пользовательскую папку
$env:PATH += ";$env:USERPROFILE\bin"
```

### **Проблемы с Docker:**
```powershell
# Проверка статуса Docker
docker info

# Перезапуск Docker Desktop
# Через системный трей или Services
```

### **Проблемы с Minikube:**
```powershell
# Очистка и перезапуск
minikube delete
minikube start --driver=docker

# Проверка логов
minikube logs
```

---

**Все инструменты установлены и готовы к использованию!** 🎉

