# 🔧 Устранение проблем с отображением подов

## 🚨 Проблема: Список подов не отображается

### 📋 Пошаговая диагностика

#### **1. Запустите диагностический скрипт:**
```powershell
cd kubernetes
.\debug-pods.ps1
```

#### **2. Проверьте backend:**
```bash
# Запуск backend
cd backend
mvn spring-boot:run

# Проверка в другом терминале
curl http://localhost:3001/api/version/config
curl http://localhost:3001/api/version/test
```

#### **3. Проверьте frontend:**
```bash
# Запуск frontend
cd client
npm start

# Откройте в браузере
http://localhost:3000
```

## 🔍 Возможные причины и решения

### **Причина 1: Kubernetes интеграция отключена**

**Симптомы:**
- Пустой список подов
- В логах: "Kubernetes интеграция отключена"

**Решение:**
```properties
# В application.properties
kubernetes.enabled=true
```

**Или через переменные окружения:**
```bash
export KUBERNETES_ENABLED=true
```

### **Причина 2: kubectl не найден**

**Симптомы:**
- Ошибка: "kubectl не найден"
- Exit code != 0

**Решение:**
```bash
# Windows
choco install kubernetes-cli

# Или скачайте с https://kubernetes.io/docs/tasks/tools/
# Добавьте в PATH
```

### **Причина 3: Minikube не запущен**

**Симптомы:**
- Ошибка подключения к кластеру
- "No resources found"

**Решение:**
```bash
# Запуск Minikube
minikube start --driver=docker --addons=ingress

# Проверка статуса
minikube status
```

### **Причина 4: Нет запущенных подов**

**Симптомы:**
- Пустой список подов
- "No resources found"

**Решение:**
```bash
# Запуск dev-tools
cd kubernetes/dev-tools
.\deploy-dev-tools.ps1

# Проверка подов
kubectl get pods --all-namespaces
```

### **Причина 5: Неправильный namespace**

**Симптомы:**
- Пустой список подов
- Поды есть в другом namespace

**Решение:**
```properties
# В application.properties
kubernetes.namespace=dev-tools

# Или через переменные окружения
export KUBERNETES_NAMESPACE=dev-tools
```

### **Причина 6: Проблемы с правами доступа**

**Симптомы:**
- Ошибка: "Forbidden"
- Exit code != 0

**Решение:**
```bash
# Проверка прав
kubectl auth can-i get pods

# Настройка RBAC (если нужно)
kubectl create clusterrolebinding dashboard-admin --clusterrole=cluster-admin --serviceaccount=default:default
```

## 🛠️ Отладка

### **1. Проверка логов backend:**
```bash
# В логах ищите:
# - "Kubernetes интеграция отключена"
# - "kubectl exit code"
# - "Ошибка выполнения kubectl команды"
```

### **2. Проверка в браузере:**
```javascript
// Откройте Developer Tools (F12)
// В Console проверьте:
fetch('http://localhost:3001/api/version/test')
  .then(response => response.json())
  .then(data => console.log(data));
```

### **3. Проверка kubectl вручную:**
```bash
# Проверка доступности
kubectl version

# Проверка подов
kubectl get pods --all-namespaces

# Проверка конкретного namespace
kubectl get pods -n dev-tools
```

## 🚀 Быстрое решение

### **Полная настройка с нуля:**

```bash
# 1. Запуск Minikube
minikube start --driver=docker --addons=ingress --memory=4096 --cpus=2

# 2. Развертывание dev-tools
cd kubernetes/dev-tools
.\deploy-dev-tools.ps1

# 3. Запуск backend с Kubernetes
cd ../../backend
export KUBERNETES_ENABLED=true
export KUBERNETES_NAMESPACE=dev-tools
mvn spring-boot:run

# 4. Запуск frontend
cd ../client
npm start
```

### **Проверка работы:**
1. Откройте http://localhost:3000
2. Нажмите кнопку "📦 Поды K8s"
3. Должны отобразиться поды из dev-tools

## 📊 Тестовые endpoints

### **Проверка конфигурации:**
```bash
curl http://localhost:3001/api/version/config
```

### **Тестирование сервиса:**
```bash
curl http://localhost:3001/api/version/test
```

### **Проверка здоровья:**
```bash
curl http://localhost:3001/api/version/health
```

### **Получение подов:**
```bash
curl http://localhost:3001/api/version/pods
```

## 🔧 Дополнительные настройки

### **Включение отладки:**
```properties
# В application.properties
logging.level.com.dashboard.service.KubernetesService=DEBUG
```

### **Настройка kubectl:**
```properties
# В application.properties
kubernetes.kubectl.path=/usr/local/bin/kubectl
```

### **Настройка namespace:**
```properties
# В application.properties
kubernetes.namespace=my-namespace
```

## 📞 Получение помощи

### **Если проблема не решается:**

1. **Запустите диагностический скрипт:**
   ```powershell
   .\debug-pods.ps1
   ```

2. **Проверьте логи backend:**
   ```bash
   tail -f logs/application.log
   ```

3. **Проверьте консоль браузера:**
   - Откройте Developer Tools (F12)
   - Посмотрите на ошибки в Console

4. **Проверьте Network tab:**
   - Посмотрите на запросы к API
   - Проверьте статус коды ответов

---

**Следуйте этим инструкциям для решения проблем с отображением подов!** 🎯
