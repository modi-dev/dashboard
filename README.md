# Server Dashboard

Система мониторинга серверов с веб-интерфейсом и API.

## 🏗️ Архитектура проекта

### Backend (Java Spring Boot)
- **Технологии**: Java 17, Spring Boot 3.2, Maven, PostgreSQL
- **Особенности**: REST API, JPA/Hibernate, WebFlux, Actuator
- **Мониторинг**: Автоматическая проверка серверов каждые 30 секунд

### Frontend (React)
- **Технологии**: React 18, Axios, CSS
- **Особенности**: Responsive UI, Real-time updates, Error handling

## 📁 Структура проекта

```
dashboard/
├── backend/                    # Java Spring Boot Backend
│   ├── src/main/java/com/dashboard/
│   │   ├── controller/         # REST контроллеры
│   │   ├── model/             # JPA модели
│   │   ├── dto/               # DTO классы
│   │   ├── repository/        # JPA репозитории
│   │   ├── service/           # Бизнес логика
│   │   └── config/            # Конфигурация
│   ├── src/main/resources/
│   │   └── application.yml     # Конфигурация
│   ├── pom.xml                # Maven конфигурация
│   └── README.md              # Документация backend
├── client/                     # React Frontend
│   ├── src/
│   │   └── App.js             # Главный компонент
│   ├── package.json           # NPM зависимости
│   └── README.md              # Документация frontend
├── src/                       # TypeScript Backend (Legacy)
└── README.md                  # Основная документация
```

## 🚀 Быстрый старт

### 1. Установка зависимостей
```bash
npm install
```

### 2. Настройка переменных окружения
Скопируйте `.env.example` в `.env` и настройте переменные:
```bash
cp .env.example .env
```

Отредактируйте `.env` файл:
```env
# Database Configuration
DB_HOST=127.0.0.1
DB_PORT=5432
DB_NAME=postgres
DB_USER=postgres
DB_PASSWORD=your_password_here

# Server Configuration
PORT=3001
NODE_ENV=development

# Monitoring Configuration
MONITOR_INTERVAL=30000
MONITOR_TIMEOUT=10000

# CORS Configuration
CORS_ORIGIN=*
```

### 3. Запуск PostgreSQL и PgAdmin4
```bash
# Через Docker
#   Создадим сеть 
docker network create postgres_net
docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=password --network postgres_net postgres
docker run -d --name pgadmin -p 8080:80 -e PGADMIN_DEFAULT_EMAIL=user@example.com -e PGADMIN_DEFAULT_PASSWORD=password --network postgres_net dpage/pgadmin4
```

### 4. Запуск сервера
```bash
npm run dev
```

### 5. Запуск клиента
```bash
cd client
npm start
```

## 📁 Структура файлов

```
dashboard/src/
├── server.js                 # Основной файл сервера
├── config/
│   └── database.js          # Конфигурация базы данных
├── models/
│   └── Server.js            # Модель сервера (Sequelize)
├── routes/
│   └── servers.js           # API маршруты для серверов
├── services/
│   └── serverMonitor.js     # Сервис мониторинга серверов
└── client/                  # React фронтенд
    └── src/
        └── App.js
```

## 🔧 Описание модулей

### `server.js` - Основной файл
- Инициализация Express приложения
- Настройка middleware (CORS, JSON)
- Подключение маршрутов
- Запуск сервера и мониторинга
- Graceful shutdown

### `config/database.js` - Конфигурация БД
- Настройка подключения к PostgreSQL
- Проверка соединения
- Синхронизация схемы БД

### `models/Server.js` - Модель сервера
- Определение структуры таблицы Server
- Валидация полей
- Настройки Sequelize

### `routes/servers.js` - API маршруты
- `GET /api/servers` - получение всех серверов
- `POST /api/servers` - создание сервера
- `GET /api/servers/:id` - получение сервера по ID
- `PUT /api/servers/:id` - обновление сервера
- `DELETE /api/servers/:id` - удаление сервера

### `services/serverMonitor.js` - Мониторинг серверов
- Класс для управления мониторингом
- Автоматическая проверка серверов
- Обновление статуса в БД
- Управление интервалами проверки

## 🚀 Преимущества новой структуры

1. **Разделение ответственности** - каждый файл отвечает за свою область
2. **Легкость тестирования** - модули можно тестировать независимо
3. **Масштабируемость** - легко добавлять новые модели и маршруты
4. **Читаемость** - код легче понимать и поддерживать
5. **Переиспользование** - модули можно использовать в других проектах

## 📋 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Информация об API |
| GET | `/api/servers` | Получить все серверы |
| POST | `/api/servers` | Создать сервер |
| GET | `/api/servers/:id` | Получить сервер по ID |
| PUT | `/api/servers/:id` | Обновить сервер |
| DELETE | `/api/servers/:id` | Удалить сервер |

## 🔄 Мониторинг

Сервис мониторинга автоматически:
- Проверяет все серверы каждые 30 секунд
- Обновляет статус (online/offline/unknown)
- Записывает время последней проверки
- Логирует результаты в консоль

### 🎯 Типы проверок серверов

#### **PostgreSQL** 
- Проверка TCP соединения на порт 5432 (по умолчанию)
- URL формат: `postgres://host:port` или `postgres://host:5432`

#### **Redis**
- Проверка TCP соединения на порт 6379 (по умолчанию)
- URL формат: `redis://host:port` или `redis://host:6379`

#### **Kafka**
- Проверка TCP соединения на порт 9092 (по умолчанию)
- URL формат: `kafka://host:port` или `kafka://host:9092`

#### **Astra Linux**
- Проверка SSH соединения на порт 22 (по умолчанию)
- URL формат: `ssh://host:port` или `ssh://host:22`

#### **Другое**
- HTTP/HTTPS проверка с поддержкой healthcheck endpoints
- URL формат: `http://host:port` или `https://host:port`
- Поддержка кастомных healthcheck путей


## 📁 **Новая структура проекта**

```
dashboard/
├── src/                          # TypeScript исходники
│   ├── config/
│   │   └── database.ts           # Конфигурация БД
│   ├── models/
│   │   └── Server.ts             # Типизированная модель
│   ├── routes/
│   │   └── servers.ts            # Типизированные маршруты
│   ├── services/
│   │   └── serverMonitor.ts     # Типизированный мониторинг
│   ├── scripts/
│   │   └── migrate.ts            # Типизированные миграции
│   ├── types/
│   │   └── index.ts              # Определения типов
│   └── server.ts                 # Основной файл сервера
├── dist/                         # Скомпилированный JavaScript
├── tsconfig.json                 # Конфигурация TypeScript
├── nodemon.json                  # Конфигурация для разработки
└── package.json                  # Обновленные скрипты
```

## 🎯 **Преимущества TypeScript**

### **1. Типобезопасность:**
```typescript
// ❌ JavaScript - ошибки во время выполнения
const server = { name: "Test", url: 123 }; // url должен быть строкой

// ✅ TypeScript - ошибки на этапе компиляции
const server: IServer = { 
  name: "Test", 
  url: "http://example.com", // ✅ Правильный тип
  type: "Postgres"           // ✅ Обязательное поле
};
```

### **2. Лучший IntelliSense:**
```typescript
// Автодополнение полей и методов
server. // IDE покажет все доступные свойства
```

### **3. Рефакторинг:**
```typescript
// Переименование свойства обновит все использования
interface IServer {
  serverName: string; // Переименовали name -> serverName
}
```

### **4. Документация в коде:**
```typescript
interface IServer {
  name: string;        // Название сервера
  url: string;        // URL для мониторинга
  type: ServerType;   // Тип сервера
  status: ServerStatus; // Текущий статус
}
```

## 🛠️ **Команды для работы**

### **Разработка:**
```bash
npm run dev          # Запуск в режиме разработки с автоперезагрузкой
```

### **Продакшн:**
```bash
npm run build        # Компиляция TypeScript в JavaScript
npm start            # Запуск скомпилированного кода
```

### **Проверка типов:**
```bash
npx tsc --noEmit     # Проверка типов без компиляции
```

## 📋 **Созданные типы**

### **Основные интерфейсы:**
```typescript
interface IServer {
  id?: number;
  name: string;
  url: string;
  type: ServerType;
  healthcheck?: string;
  status: ServerStatus;
  lastChecked?: Date;
  createdAt?: Date;
  updatedAt?: Date;
}

type ServerType = 'Postgres' | 'Redis' | 'Kafka' | 'Astra Linux' | 'Другое';
type ServerStatus = 'online' | 'offline' | 'unknown';
```

### **API типы:**
```typescript
interface IApiResponse<T = any> {
  success: boolean;
  data?: T | undefined;
  error?: string | undefined;
  message?: string | undefined;
}
```

## 🔧 **Конфигурация TypeScript**

### **tsconfig.json:**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "commonjs",
    "strict": true,
    "noImplicitAny": true,
    "noImplicitReturns": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true
  }
}
```