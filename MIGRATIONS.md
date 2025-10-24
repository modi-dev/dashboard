# 🔄 Система миграций базы данных

## ❌ **Проблема с `force: true`**

### **Что происходило раньше:**
```javascript
await sequelize.sync({ force: true });
```

**Результат:**
1. **DROP TABLE** - удаляет существующую таблицу
2. **CREATE TABLE** - создает новую таблицу
3. **Все данные теряются** ❌

## ✅ **Решение: Система миграций**

### **Новый подход:**
```javascript
await sequelize.sync({ force: false }); // Безопасная синхронизация
await runMigrations(); // Применяем миграции
```

**Результат:**
1. **ALTER TABLE** - добавляет новые колонки
2. **Данные сохраняются** ✅
3. **Безопасное обновление схемы** ✅

## 📁 **Структура миграций**

```
dashboard/
├── scripts/
│   └── migrate.js          # Система миграций
├── migrations/
│   └── 001-add-type-and-healthcheck.js  # Миграция
└── config/
    └── database.js         # Обновленная конфигурация
```

## 🛠️ **Как работают миграции**

### **1. Проверка существования колонок:**
```sql
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'Servers' 
AND column_name IN ('type', 'healthcheck')
```

### **2. Безопасное добавление колонок:**
```sql
-- Добавляем только если колонки нет
ALTER TABLE "Servers" 
ADD COLUMN "type" VARCHAR(255) DEFAULT 'Другое' NOT NULL;

ALTER TABLE "Servers" 
ADD COLUMN "healthcheck" VARCHAR(255);
```

### **3. Откат миграций:**
```sql
ALTER TABLE "Servers" DROP COLUMN IF EXISTS "healthcheck";
ALTER TABLE "Servers" DROP COLUMN IF EXISTS "type";
```

## 🚀 **Преимущества новой системы**

### **✅ Безопасность данных:**
- Данные не удаляются при обновлении схемы
- Можно откатить изменения
- Контролируемое обновление

### **✅ Гибкость:**
- Можно добавлять новые миграции
- Поддержка отката изменений
- Версионирование схемы БД

### **✅ Производственная готовность:**
- Безопасно для продакшена
- Можно применять на существующих данных
- Логирование изменений

## 📋 **Команды для работы с миграциями**

### **Запуск миграций:**
```bash
node scripts/migrate.js
```

### **Откат миграций:**
```javascript
const { rollbackMigrations } = require('./scripts/migrate');
await rollbackMigrations();
```

## 🔧 **Создание новых миграций**

### **1. Создайте файл миграции:**
```javascript
// migrations/002-add-new-field.js
module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.addColumn('Servers', 'newField', {
      type: DataTypes.STRING,
      allowNull: true
    });
  },
  down: async (queryInterface, Sequelize) => {
    await queryInterface.removeColumn('Servers', 'newField');
  }
};
```

### **2. Добавьте в список миграций:**
```javascript
// scripts/migrate.js
const migrations = [
  // ... существующие миграции
  {
    name: '002-add-new-field',
    up: async () => { /* логика миграции */ },
    down: async () => { /* логика отката */ }
  }
];
```

## 🎯 **Результат**

Теперь при изменении модели:
- ✅ **Данные сохраняются**
- ✅ **Схема обновляется безопасно**
- ✅ **Можно откатить изменения**
- ✅ **Готово для продакшена**
