import { Sequelize } from 'sequelize';
import { IDatabaseConfig } from '../types';
import dotenv from 'dotenv';

// Загружаем переменные окружения
dotenv.config();

// Конфигурация базы данных из переменных окружения
const dbConfig: IDatabaseConfig = {
  host: process.env.DB_HOST || '127.0.0.1',
  port: parseInt(process.env.DB_PORT || '5432'),
  database: process.env.DB_NAME || 'postgres',
  username: process.env.DB_USER || 'postgres',
  password: process.env.DB_PASSWORD || 'password'
};

// Создание подключения к БД
const sequelize = new Sequelize(
  `postgres://${dbConfig.username}:${dbConfig.password}@${dbConfig.host}:${dbConfig.port}/${dbConfig.database}`
);

// Проверка подключения к БД
export const connectDB = async (): Promise<Sequelize> => {
  try {
    await sequelize.authenticate();
    console.log('Database connection has been established successfully.');
    return sequelize;
  } catch (err) {
    console.error('Unable to connect to the database:', err);
    throw err;
  }
};

// Синхронизация БД
export const syncDatabase = async (): Promise<void> => {
  try {
    // Синхронизируем базовую структуру без force
    await sequelize.sync({ force: false });
    
    // Запускаем миграции для безопасного обновления схемы
    const { runMigrations } = await import('../scripts/migrate');
    await runMigrations();
    
    console.log('Database synchronized successfully.');
  } catch (err) {
    console.error('Error synchronizing database:', err);
    throw err;
  }
};

export { sequelize };
export default sequelize;
