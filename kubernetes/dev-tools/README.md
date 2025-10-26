# 🛠️ Dev-Tools для Minikube

Набор полезных сервисов для разработки и тестирования в Kubernetes.

## 📦 Включенные сервисы

### **🌐 Web сервисы:**
- **Nginx** - веб-сервер для тестирования
- **Ingress** - маршрутизация трафика

### **🗄️ Базы данных:**
- **PostgreSQL** - реляционная БД
- **MongoDB** - документная БД
- **Redis** - кэш и хранилище ключ-значение

### **📊 Мониторинг:**
- **Prometheus** - сбор метрик
- **Grafana** - визуализация метрик

### **🔍 Логирование:**
- **Elasticsearch** - поиск и аналитика
- **Kibana** - визуализация логов

## 🚀 Быстрый старт

### **1. Запуск Minikube:**
```powershell
minikube start --driver=docker --addons=ingress --memory=4096 --cpus=2
```

### **2. Развертывание Dev-Tools:**
```powershell
cd kubernetes/dev-tools
.\deploy-dev-tools.ps1
```

### **3. Доступ к сервисам:**
Добавьте в `C:\Windows\System32\drivers\etc\hosts`:
```
127.0.0.1 nginx-dev.local
127.0.0.1 prometheus.local
127.0.0.1 grafana.local
127.0.0.1 elasticsearch.local
127.0.0.1 kibana.local
```

## 🔗 Ссылки для доступа

| Сервис | URL | Описание |
|--------|-----|----------|
| **Nginx** | http://nginx-dev.local | Веб-сервер |
| **Prometheus** | http://prometheus.local | Метрики |
| **Grafana** | http://grafana.local | Дашборды (admin/admin) |
| **Elasticsearch** | http://elasticsearch.local | Поиск |
| **Kibana** | http://kibana.local | Логи |

## 🔧 Полезные команды

### **Просмотр статуса:**
```powershell
# Все ресурсы
kubectl get all -n dev-tools

# Подробная информация
kubectl describe pods -n dev-tools

# Логи сервисов
kubectl logs -f deployment/nginx-dev -n dev-tools
kubectl logs -f deployment/redis-dev -n dev-tools
kubectl logs -f deployment/postgres-dev -n dev-tools
```

### **Подключение к базам данных:**
```powershell
# PostgreSQL
kubectl run postgres-client --rm -i --tty --restart=Never --image=postgres:15-alpine -- psql -h postgres-dev-service -U postgres -d dev_db

# MongoDB
kubectl run mongodb-client --rm -i --tty --restart=Never --image=mongo:7 -- mongosh mongodb://mongodb-dev-service:27017

# Redis
kubectl run redis-client --rm -i --tty --restart=Never --image=redis:7-alpine -- redis-cli -h redis-dev-service
```

### **Порт-форвардинг для тестирования:**
```powershell
# Nginx
kubectl port-forward service/nginx-dev-service 8080:80 -n dev-tools

# PostgreSQL
kubectl port-forward service/postgres-dev-service 5432:5432 -n dev-tools

# Redis
kubectl port-forward service/redis-dev-service 6379:6379 -n dev-tools

# MongoDB
kubectl port-forward service/mongodb-dev-service 27017:27017 -n dev-tools
```

## 📊 Мониторинг и логирование

### **Prometheus:**
- Собирает метрики с Kubernetes кластера
- Доступен по адресу: http://prometheus.local
- Настроен для мониторинга подов и нод

### **Grafana:**
- Визуализация метрик из Prometheus
- Доступен по адресу: http://grafana.local
- Логин: admin, пароль: admin

### **Elasticsearch:**
- Поиск и аналитика данных
- Доступен по адресу: http://elasticsearch.local
- API: http://elasticsearch.local/_cluster/health

### **Kibana:**
- Визуализация данных из Elasticsearch
- Доступен по адресу: http://kibana.local
- Настроен для работы с Elasticsearch

## 🗄️ Базы данных

### **PostgreSQL:**
- **Порт:** 5432
- **База:** dev_db
- **Пользователь:** postgres
- **Пароль:** postgres
- **PVC:** 5Gi

### **MongoDB:**
- **Порт:** 27017
- **База:** dev_db
- **Пользователь:** admin
- **Пароль:** mongodb
- **PVC:** 5Gi

### **Redis:**
- **Порт:** 6379
- **Конфигурация:** maxmemory 256mb
- **PVC:** 1Gi

## 🔍 Troubleshooting

### **Проблемы с подами:**
```powershell
# Проверка статуса
kubectl get pods -n dev-tools

# Подробная информация
kubectl describe pod <pod-name> -n dev-tools

# Логи
kubectl logs <pod-name> -n dev-tools
```

### **Проблемы с сетью:**
```powershell
# Проверка сервисов
kubectl get services -n dev-tools

# Проверка Ingress
kubectl get ingress -n dev-tools
```

### **Проблемы с хранилищем:**
```powershell
# Проверка PVC
kubectl get pvc -n dev-tools

# Проверка PV
kubectl get pv
```

## 🧹 Очистка

### **Удаление всех Dev-Tools:**
```powershell
kubectl delete namespace dev-tools
```

### **Удаление отдельных сервисов:**
```powershell
kubectl delete -f nginx-deployment.yaml
kubectl delete -f redis-deployment.yaml
kubectl delete -f postgres-dev.yaml
kubectl delete -f mongodb-dev.yaml
kubectl delete -f grafana-prometheus.yaml
kubectl delete -f elasticsearch-kibana.yaml
```

## 📚 Дополнительные ресурсы

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Minikube Documentation](https://minikube.sigs.k8s.io/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Elasticsearch Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/)

---

**Dev-Tools готовы к использованию!** 🎉

