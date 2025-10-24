const express = require('express');
const { connectDB, syncDatabase } = require('./config/database');
const serverMonitor = require('./services/serverMonitor');

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(express.json());

// CORS для работы с фронтендом
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
  if (req.method === 'OPTIONS') {
    res.sendStatus(200);
  } else {
    next();
  }
});

// Маршруты
app.use('/api/servers', require('./routes/servers'));

// Базовый маршрут
app.get('/', (req, res) => {
  res.json({ 
    message: 'Server Dashboard API', 
    version: '1.0.0',
    endpoints: {
      servers: '/api/servers'
    }
  });
});

// Обработка ошибок
app.use((err, req, res, next) => {
  console.error('Unhandled error:', err);
  res.status(500).json({ error: 'Internal server error' });
});

// Запуск сервера
const startServer = async () => {
  try {
    // Подключение к БД
    await connectDB();
    
    // Синхронизация БД
    await syncDatabase();
    
    // Запуск мониторинга серверов
    serverMonitor.start();
    
    // Запуск сервера
    app.listen(PORT, () => {
      console.log(`🚀 Server is running on http://localhost:${PORT}`);
      console.log(`📊 Server monitoring started`);
    });
    
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
};

// Graceful shutdown
process.on('SIGINT', () => {
  console.log('\n🛑 Shutting down server...');
  serverMonitor.stop();
  process.exit(0);
});

process.on('SIGTERM', () => {
  console.log('\n🛑 Shutting down server...');
  serverMonitor.stop();
  process.exit(0);
});

startServer();