# 🎨 Интеграция информации о подах Kubernetes в UI

## 📋 Обзор

Информация о версиях подов из скрипта `version.sh` была интегрирована в React UI приложение. Теперь пользователи могут просматривать информацию о Kubernetes подах прямо в веб-интерфейсе.

## 🏗️ Архитектура интеграции

### **Компоненты:**

1. **`PodInfo.js`** - основной компонент для отображения информации о подах
2. **`PodInfo.css`** - стили для компонента (в стиле оригинального скрипта)
3. **`App.js`** - интеграция компонента в главное приложение

### **Функциональность:**

- ✅ Отображение таблицы с информацией о подах
- ✅ Загрузка данных через REST API
- ✅ Кнопки управления (обновление, HTML версия, проверка API)
- ✅ Адаптивный дизайн для мобильных устройств
- ✅ Обработка ошибок и состояний загрузки

## 🎨 UI Компоненты

### **1. Кнопка переключения подов**
```jsx
<button 
  className="btn btn-info pod-info-btn"
  onClick={togglePodInfo}
  title={showPodInfo ? 'Скрыть информацию о подах' : 'Показать информацию о подах'}
>
  <span className="btn-icon">📦</span>
  {showPodInfo ? 'Скрыть поды' : 'Поды K8s'}
</button>
```

### **2. Компонент PodInfo**
```jsx
<PodInfo />
```

### **3. Таблица с информацией о подах**
- Имя пода
- Версия образа
- Ms Branch
- Config Branch
- GC Options
- Дата создания
- Порт
- Ресурсы (CPU, RAM)

## 🔧 API Интеграция

### **Endpoints:**
- `GET /api/version/pods` - получение JSON данных о подах
- `GET /api/version/namespace` - получение текущего namespace
- `GET /api/version/html` - получение HTML страницы
- `GET /api/version/health` - проверка доступности Kubernetes API

### **Обработка данных:**
```javascript
const fetchPods = async () => {
  try {
    const podsResponse = await fetch('http://localhost:3001/api/version/pods');
    const podsData = await podsResponse.json();
    setPods(podsData);
  } catch (err) {
    setError(err.message);
  }
};
```

## 🎨 Стили и дизайн

### **Цветовая схема:**
- **Заголовок:** Градиент от #667eea до #764ba2
- **Таблица:** Стиль IKSWEB (как в оригинальном скрипте)
- **Кнопки:** Фиолетовый цвет (#6f42c1) для кнопки подов
- **Ресурсы:** Светло-серый фон с рамками

### **Адаптивность:**
- Мобильные устройства: вертикальная компоновка
- Планшеты: адаптивная сетка
- Десктоп: полная функциональность

## 🚀 Использование

### **1. Включение Kubernetes интеграции в backend:**
```bash
export KUBERNETES_ENABLED=true
export KUBERNETES_NAMESPACE=dev-tools
```

### **2. Запуск приложения:**
```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd client
npm start
```

### **3. Использование в UI:**
1. Откройте приложение в браузере
2. Нажмите кнопку "Поды K8s" в заголовке
3. Просматривайте информацию о подах
4. Используйте кнопки управления

## 🔧 Функции управления

### **Кнопки в компоненте PodInfo:**

| Кнопка | Функция | Описание |
|--------|---------|----------|
| **🔄 Обновить** | `fetchPods()` | Загружает свежие данные о подах |
| **🌐 HTML версия** | `fetchHtmlPage()` | Открывает HTML версию в новом окне |
| **❤️ Проверить API** | `checkKubernetesHealth()` | Проверяет доступность Kubernetes API |

### **Кнопка переключения в App.js:**
- **📦 Поды K8s** - показывает/скрывает компонент PodInfo

## 📊 Отображаемая информация

### **Метаинформация:**
- Namespace
- Время последнего обновления
- Количество подов

### **Данные о подах:**
- **Имя** - из `metadata.labels.app`
- **Версия** - из `spec.containers[].image` (с удалением registry)
- **Ms Branch** - из `metadata.annotations.ms-branch`
- **Config Branch** - из `metadata.annotations.config-branch`
- **GC Options** - из `spec.containers[].env[].value` (JAVA_TOOL_OPTIONS)
- **Дата создания** - из `metadata.creationTimestamp`
- **Порт** - из `spec.containers[].ports[].containerPort`
- **Ресурсы** - CPU и RAM из `spec.containers[].resources.requests`

## 🎯 Состояния компонента

### **1. Загрузка:**
```jsx
<div className="loading-spinner">Загрузка...</div>
```

### **2. Ошибка:**
```jsx
<div className="error-message">
  <p>❌ Ошибка: {error}</p>
  <button onClick={fetchPods}>🔄 Попробовать снова</button>
</div>
```

### **3. Пустое состояние:**
```jsx
<div className="no-pods-message">
  <p>📭 Поды не найдены</p>
  <p>Убедитесь, что Kubernetes интеграция включена</p>
</div>
```

### **4. Данные:**
```jsx
<table className="pod-info-table">
  {/* Таблица с данными о подах */}
</table>
```

## 🔄 Автоматическое обновление

### **Текущая реализация:**
- Ручное обновление по кнопке
- Нет автоматического обновления

### **Возможные улучшения:**
```javascript
// Автоматическое обновление каждые 30 секунд
useEffect(() => {
  const interval = setInterval(fetchPods, 30000);
  return () => clearInterval(interval);
}, []);
```

## 📱 Адаптивность

### **Мобильные устройства (< 768px):**
- Вертикальная компоновка кнопок
- Уменьшенные размеры таблицы
- Скрытие некоторых колонок

### **Планшеты (768px - 1024px):**
- Адаптивная сетка
- Оптимизированные размеры

### **Десктоп (> 1024px):**
- Полная функциональность
- Все колонки таблицы

## 🧪 Тестирование

### **Ручное тестирование:**
1. Включите Kubernetes интеграцию в backend
2. Запустите приложение
3. Нажмите кнопку "Поды K8s"
4. Проверьте отображение данных
5. Протестируйте кнопки управления

### **Тестовые сценарии:**
- ✅ Загрузка при отключенном Kubernetes
- ✅ Отображение данных о подах
- ✅ Обработка ошибок API
- ✅ Адаптивность на разных устройствах

## 🔧 Troubleshooting

### **Проблемы с API:**
```javascript
// Проверка доступности backend
fetch('http://localhost:3001/api/version/health')
  .then(response => response.text())
  .then(data => console.log(data));
```

### **Проблемы с CORS:**
```javascript
// В backend добавить CORS для всех origins
@CrossOrigin(origins = "*")
```

### **Проблемы с Kubernetes:**
- Проверьте, что `KUBERNETES_ENABLED=true`
- Убедитесь, что kubectl доступен
- Проверьте права доступа к кластеру

## 📚 Дополнительные возможности

### **1. Фильтрация подов:**
```javascript
const [filter, setFilter] = useState('');
const filteredPods = pods.filter(pod => 
  pod.name.toLowerCase().includes(filter.toLowerCase())
);
```

### **2. Сортировка:**
```javascript
const [sortBy, setSortBy] = useState('name');
const sortedPods = pods.sort((a, b) => 
  a[sortBy].localeCompare(b[sortBy])
);
```

### **3. Экспорт данных:**
```javascript
const exportToCSV = () => {
  const csv = pods.map(pod => 
    `${pod.name},${pod.version},${pod.port}`
  ).join('\n');
  // Скачивание CSV файла
};
```

---

**Интеграция информации о подах Kubernetes в UI завершена!** 🎉
