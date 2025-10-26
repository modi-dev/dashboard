# PowerShell скрипт для диагностики проблем с подами

Write-Host "🔍 Диагностика проблем с отображением подов..." -ForegroundColor Green

# Проверка 1: Backend запущен?
Write-Host "`n1. Проверка backend..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3001/api/version/config" -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        $config = $response.Content | ConvertFrom-Json
        Write-Host "✅ Backend запущен" -ForegroundColor Green
        Write-Host "   Kubernetes enabled: $($config.enabled)" -ForegroundColor White
        Write-Host "   Namespace: $($config.namespace)" -ForegroundColor White
        Write-Host "   Kubectl path: $($config.kubectlPath)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Backend не запущен или недоступен" -ForegroundColor Red
    Write-Host "   Запустите: cd backend && mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# Проверка 2: Kubernetes API доступен?
Write-Host "`n2. Проверка Kubernetes API..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3001/api/version/health" -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Kubernetes API доступен" -ForegroundColor Green
        Write-Host "   $($response.Content)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Kubernetes API недоступен" -ForegroundColor Red
    Write-Host "   Ошибка: $($_.Exception.Message)" -ForegroundColor White
}

# Проверка 3: Тестовый endpoint
Write-Host "`n3. Тестирование Kubernetes сервиса..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3001/api/version/test" -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        $testResult = $response.Content | ConvertFrom-Json
        Write-Host "✅ Тест прошел успешно" -ForegroundColor Green
        Write-Host "   Статус: $($testResult.status)" -ForegroundColor White
        Write-Host "   Количество подов: $($testResult.podsCount)" -ForegroundColor White
        Write-Host "   Сообщение: $($testResult.message)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Тест не прошел" -ForegroundColor Red
    Write-Host "   Ошибка: $($_.Exception.Message)" -ForegroundColor White
}

# Проверка 4: kubectl доступен?
Write-Host "`n4. Проверка kubectl..." -ForegroundColor Yellow
try {
    $kubectlVersion = kubectl version --client --output=yaml 2>$null
    if ($kubectlVersion) {
        Write-Host "✅ kubectl доступен" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ kubectl не найден" -ForegroundColor Red
    Write-Host "   Установите kubectl или проверьте PATH" -ForegroundColor Yellow
}

# Проверка 5: Minikube запущен?
Write-Host "`n5. Проверка Minikube..." -ForegroundColor Yellow
try {
    $minikubeStatus = minikube status 2>$null
    if ($minikubeStatus -match "Running") {
        Write-Host "✅ Minikube запущен" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Minikube не запущен" -ForegroundColor Yellow
        Write-Host "   Запустите: minikube start --driver=docker --addons=ingress" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Minikube не найден" -ForegroundColor Red
    Write-Host "   Установите Minikube" -ForegroundColor Yellow
}

# Проверка 6: Есть ли поды в кластере?
Write-Host "`n6. Проверка подов в кластере..." -ForegroundColor Yellow
try {
    $pods = kubectl get pods --all-namespaces --field-selector=status.phase==Running -o json 2>$null | ConvertFrom-Json
    if ($pods.items.Count -gt 0) {
        Write-Host "✅ Найдено $($pods.items.Count) запущенных подов" -ForegroundColor Green
        foreach ($pod in $pods.items) {
            Write-Host "   - $($pod.metadata.name) в namespace $($pod.metadata.namespace)" -ForegroundColor White
        }
    } else {
        Write-Host "⚠️ Запущенных подов не найдено" -ForegroundColor Yellow
        Write-Host "   Запустите dev-tools: .\deploy-dev-tools.ps1" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Не удалось получить информацию о подах" -ForegroundColor Red
    Write-Host "   Ошибка: $($_.Exception.Message)" -ForegroundColor White
}

# Проверка 7: Frontend доступен?
Write-Host "`n7. Проверка frontend..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Frontend доступен" -ForegroundColor Green
        Write-Host "   Откройте: http://localhost:3000" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Frontend не запущен" -ForegroundColor Red
    Write-Host "   Запустите: cd client && npm start" -ForegroundColor Yellow
}

Write-Host "`n🎯 Рекомендации:" -ForegroundColor Cyan
Write-Host "1. Убедитесь, что backend запущен с kubernetes.enabled=true" -ForegroundColor White
Write-Host "2. Проверьте, что kubectl доступен и настроен" -ForegroundColor White
Write-Host "3. Запустите Minikube и dev-tools для тестирования" -ForegroundColor White
Write-Host "4. Проверьте логи backend для детальной информации" -ForegroundColor White

Write-Host "`n✅ Диагностика завершена!" -ForegroundColor Green
