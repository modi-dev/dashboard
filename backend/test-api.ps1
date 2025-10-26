# PowerShell скрипт для тестирования API

Write-Host "🔍 Тестирование API /api/version/pods..." -ForegroundColor Green

try {
    $response = Invoke-WebRequest -Uri "http://localhost:3001/api/version/pods" -UseBasicParsing
    Write-Host "✅ API доступен, статус: $($response.StatusCode)" -ForegroundColor Green
    
    $pods = $response.Content | ConvertFrom-Json
    Write-Host "📦 Найдено подов: $($pods.Count)" -ForegroundColor Cyan
    
    if ($pods.Count -gt 0) {
        Write-Host "`n📋 Список подов:" -ForegroundColor Yellow
        foreach ($pod in $pods) {
            Write-Host "  - $($pod.name): $($pod.version)" -ForegroundColor White
        }
    } else {
        Write-Host "⚠️ Поды не найдены" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Ошибка API: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🔍 Тестирование API /api/version/test..." -ForegroundColor Green

try {
    $response = Invoke-WebRequest -Uri "http://localhost:3001/api/version/test" -UseBasicParsing
    Write-Host "✅ Test API доступен, статус: $($response.StatusCode)" -ForegroundColor Green
    
    $testResult = $response.Content | ConvertFrom-Json
    Write-Host "📊 Результат теста:" -ForegroundColor Cyan
    Write-Host "  Статус: $($testResult.status)" -ForegroundColor White
    Write-Host "  Kubernetes enabled: $($testResult.kubernetesEnabled)" -ForegroundColor White
    Write-Host "  Namespace: $($testResult.namespace)" -ForegroundColor White
    Write-Host "  Количество подов: $($testResult.podsCount)" -ForegroundColor White
    
} catch {
    Write-Host "❌ Ошибка Test API: $($_.Exception.Message)" -ForegroundColor Red
}
