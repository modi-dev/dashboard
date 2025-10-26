# 🔧 Интеграция version.sh в Backend

## 📋 Обзор

Функциональность из скрипта `version.sh` была интегрирована в Spring Boot backend приложение. Теперь вместо выполнения bash скрипта можно использовать REST API для получения информации о подах Kubernetes.

## 🏗️ Архитектура интеграции

### **Компоненты:**

1. **`PodInfo.java`** - модель данных для информации о поде
2. **`KubernetesService.java`** - сервис для работы с Kubernetes API
3. **`VersionController.java`** - REST контроллер для API endpoints
4. **`KubernetesConfig.java`** - конфигурация Kubernetes

### **Функциональность:**

- ✅ Получение информации о запущенных подах
- ✅ Генерация HTML страницы (как в оригинальном скрипте)
- ✅ Парсинг JSON вывода kubectl
- ✅ Извлечение метаданных подов (версии, ветки, ресурсы)
- ✅ Поддержка конфигурации через application.properties

## 🚀 API Endpoints

### **1. Получение информации о подах (JSON)**
```http
GET /api/version/pods
```

**Ответ:**
```json
[
  {
    "name": "my-app",
    "version": "1.2.3",
    "msBranch": "main",
    "configBranch": "prod",
    "gcOptions": "-XX:+UseG1GC",
    "creationDate": "2024-01-15 10:30:00",
    "port": 8080,
    "cpuRequest": "500m",
    "memoryRequest": "1Gi"
  }
]
```

### **2. HTML страница (как в оригинальном скрипте)**
```http
GET /api/version/html
```

**Ответ:** HTML страница с таблицей подов

### **3. Текущий namespace**
```http
GET /api/version/namespace
```

**Ответ:**
```json
"default"
```

### **4. Краткая информация о подах**
```http
GET /api/version/summary
```

**Ответ:** JSON с основной информацией (без деталей ресурсов)

### **5. Информация о конкретном поде**
```http
GET /api/version/pods/{name}
```

**Ответ:** JSON с информацией о поде

### **6. Проверка доступности Kubernetes**
```http
GET /api/version/health
```

**Ответ:**
```json
"Kubernetes API доступен. Namespace: default"
```

## ⚙️ Конфигурация

### **application.properties:**
```properties
# Kubernetes configuration
kubernetes.namespace=${KUBERNETES_NAMESPACE:default}
kubernetes.kubectl.path=${KUBERNETES_KUBECTL_PATH:kubectl}
kubernetes.enabled=${KUBERNETES_ENABLED:false}
```

### **Переменные окружения:**
```bash
# Включение Kubernetes интеграции
export KUBERNETES_ENABLED=true

# Указание namespace
export KUBERNETES_NAMESPACE=my-namespace

# Путь к kubectl (если не в PATH)
export KUBERNETES_KUBECTL_PATH=/usr/local/bin/kubectl
```

## 🔧 Использование

### **1. Включение интеграции:**
```bash
# Установка переменной окружения
export KUBERNETES_ENABLED=true

# Или в application.properties
kubernetes.enabled=true
```

### **2. Запуск приложения:**
```bash
mvn spring-boot:run
```

### **3. Получение HTML страницы:**
```bash
curl http://localhost:3001/api/version/html > version.html
```

### **4. Получение JSON данных:**
```bash
curl http://localhost:3001/api/version/pods
```

## 🆚 Сравнение с оригинальным скриптом

| Функция | version.sh | Backend API |
|---------|------------|-------------|
| **Выполнение** | `./version.sh` | `GET /api/version/html` |
| **Формат вывода** | HTML файл | HTML в HTTP ответе |
| **JSON данные** | ❌ | ✅ `GET /api/version/pods` |
| **Конфигурация** | Переменные в скрипте | application.properties |
| **Логирование** | echo | SLF4J Logger |
| **Обработка ошибок** | ❌ | ✅ Try-catch блоки |
| **Тестирование** | ❌ | ✅ JUnit тесты |
| **Мониторинг** | ❌ | ✅ Spring Actuator |

## 🔍 Парсинг данных

### **Извлекаемые поля:**
- **name** - из `metadata.labels.app`
- **version** - из `spec.containers[].image` (с удалением registry)
- **msBranch** - из `metadata.annotations.ms-branch`
- **configBranch** - из `metadata.annotations.config-branch`
- **gcOptions** - из `spec.containers[].env[].value` (JAVA_TOOL_OPTIONS)
- **creationDate** - из `metadata.creationTimestamp`
- **port** - из `spec.containers[].ports[].containerPort`
- **cpuRequest** - из `spec.containers[].resources.requests.cpu`
- **memoryRequest** - из `spec.containers[].resources.requests.memory`

### **Обработка registry:**
```java
// Удаление registry из имени образа (как в оригинальном скрипте)
image = image.replaceAll("pcss-prod[^:]*:", "")
           .replaceAll("nexus[^:]*:", "")
           .replaceAll("docker[^:]*:", "");
```

## 🧪 Тестирование

### **Запуск тестов:**
```bash
mvn test -Dtest=KubernetesServiceTest
```

### **Тестируемые сценарии:**
- ✅ Получение подов при отключенном Kubernetes
- ✅ Генерация HTML страницы
- ✅ Проверка CSS стилей
- ✅ Проверка информации о namespace

## 🚀 Развертывание

### **1. Локальная разработка:**
```bash
# Включение Kubernetes
export KUBERNETES_ENABLED=true
export KUBERNETES_NAMESPACE=dev-tools

# Запуск
mvn spring-boot:run
```

### **2. Docker:**
```dockerfile
# В Dockerfile
ENV KUBERNETES_ENABLED=true
ENV KUBERNETES_NAMESPACE=default
```

### **3. Kubernetes:**
```yaml
# В deployment.yaml
env:
- name: KUBERNETES_ENABLED
  value: "true"
- name: KUBERNETES_NAMESPACE
  value: "server-dashboard"
```

## 🔧 Troubleshooting

### **Проблемы с kubectl:**
```bash
# Проверка доступности kubectl
kubectl version --client

# Проверка доступа к кластеру
kubectl get pods
```

### **Проблемы с правами:**
```bash
# Проверка прав доступа
kubectl auth can-i get pods

# Настройка RBAC (если нужно)
kubectl create clusterrolebinding dashboard-admin --clusterrole=cluster-admin --serviceaccount=default:default
```

### **Логирование:**
```bash
# Просмотр логов
kubectl logs deployment/server-dashboard-backend

# Увеличение уровня логирования
export LOG_LEVEL_DASHBOARD=DEBUG
```

## 📚 Дополнительные возможности

### **1. Кэширование:**
```java
@Cacheable("pods")
public List<PodInfo> getRunningPods() {
    // Реализация с кэшированием
}
```

### **2. WebSocket уведомления:**
```java
@EventListener
public void handlePodChange(PodChangeEvent event) {
    // Отправка уведомлений через WebSocket
}
```

### **3. Метрики Prometheus:**
```java
@Timed(name = "kubernetes.pods.fetch")
public List<PodInfo> getRunningPods() {
    // Метрики для мониторинга
}
```

---

**Интеграция version.sh в backend завершена!** 🎉
