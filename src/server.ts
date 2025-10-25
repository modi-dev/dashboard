import express, { Request, Response, NextFunction } from 'express';
import { connectDB, syncDatabase } from './config/database';
import serverMonitor from './services/serverMonitor';
import serversRouter from './routes/servers';
import dotenv from 'dotenv';

// Загружаем переменные окружения
dotenv.config();

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(express.json());

// CORS для работы с фронтендом
app.use((req: Request, res: Response, next: NextFunction) => {
  res.header('Access-Control-Allow-Origin', process.env.CORS_ORIGIN || '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
  if (req.method === 'OPTIONS') {
    res.sendStatus(200);
  } else {
    next();
  }
});

// Маршруты
app.use('/api/servers', serversRouter);

// Базовый маршрут
app.get('/', (_req: Request, res: Response) => {
  res.json({ 
    message: 'Server Dashboard API', 
    version: '1.0.0',
    endpoints: {
      servers: '/api/servers'
    }
  });
});

// Обработка ошибок
app.use((err: Error, _req: Request, res: Response, _next: NextFunction) => {
  console.error('Unhandled error:', err);
  res.status(500).json({ 
    success: false,
    error: 'Internal server error',
    message: 'An unexpected error occurred'
  });
});

// Запуск сервера
const startServer = async (): Promise<void> => {
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
