# 🔧 Ручные команды для развертывания Dev-Tools

Если скрипты не работают, выполните команды вручную:

## 📋 Пошаговые команды

### **1. Проверка Minikube:**
```powershell
minikube status
```

### **2. Создание namespace:**
```powershell
kubectl create namespace dev-tools
```

### **3. Развертывание сервисов:**
```powershell
# Nginx
kubectl apply -f nginx-deployment.yaml

# Redis
kubectl apply -f redis-deployment.yaml

# PostgreSQL
kubectl apply -f postgres-dev.yaml

# MongoDB
kubectl apply -f mongodb-dev.yaml

# Prometheus + Grafana
kubectl apply -f grafana-prometheus.yaml

# Elasticsearch + Kibana
kubectl apply -f elasticsearch-kibana.yaml
```

### **4. Проверка статуса:**
```powershell
kubectl get all -n dev-tools
```

### **5. Ожидание готовности:**
```powershell
kubectl wait --for=condition=ready pod -l app=nginx-dev -n dev-tools --timeout=120s
kubectl wait --for=condition=ready pod -l app=redis-dev -n dev-tools --timeout=120s
kubectl wait --for=condition=ready pod -l app=postgres-dev -n dev-tools --timeout=120s
kubectl wait --for=condition=ready pod -l app=mongodb-dev -n dev-tools --timeout=120s
```

## 🌐 Настройка доступа

### **Добавьте в hosts файл:**
```
127.0.0.1 nginx-dev.local
127.0.0.1 prometheus.local
127.0.0.1 grafana.local
127.0.0.1 elasticsearch.local
127.0.0.1 kibana.local
```

### **Ссылки для доступа:**
- **Nginx:** http://nginx-dev.local
- **Prometheus:** http://prometheus.local
- **Grafana:** http://grafana.local (admin/admin)
- **Elasticsearch:** http://elasticsearch.local
- **Kibana:** http://kibana.local

## 🔧 Полезные команды

### **Просмотр логов:**
```powershell
kubectl logs -f deployment/nginx-dev -n dev-tools
kubectl logs -f deployment/redis-dev -n dev-tools
kubectl logs -f deployment/postgres-dev -n dev-tools
kubectl logs -f deployment/mongodb-dev -n dev-tools
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

### **Порт-форвардинг:**
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

