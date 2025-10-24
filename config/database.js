const { Sequelize } = require('sequelize');

// Настройка подключения к БД
const sequelize = new Sequelize('postgres://postgres:password@127.0.0.1:5432/postgres');

// Проверка подключения к БД
const connectDB = async () => {
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
const syncDatabase = async () => {
  try {
    // Принудительно пересоздаем таблицы для добавления новых колонок
    await sequelize.sync({ force: true });
    console.log('Database synchronized successfully.');
  } catch (err) {
    console.error('Error synchronizing database:', err);
    throw err;
  }
};

module.exports = {
  sequelize,
  connectDB,
  syncDatabase
};
