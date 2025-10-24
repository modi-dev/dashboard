const { sequelize } = require('../config/database');

// Список миграций
const migrations = [
  {
    name: '001-add-type-and-healthcheck',
    up: async () => {
      console.log('Running migration: Add type and healthcheck columns');
      
      // Проверяем существование колонок
      const [results] = await sequelize.query(`
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'Servers' 
        AND column_name IN ('type', 'healthcheck')
      `);
      
      const existingColumns = results.map(row => row.column_name);
      
      // Добавляем колонку type если её нет
      if (!existingColumns.includes('type')) {
        console.log('Adding type column...');
        await sequelize.query(`
          ALTER TABLE "Servers" 
          ADD COLUMN "type" VARCHAR(255) DEFAULT 'Другое' NOT NULL
        `);
      }
      
      // Добавляем колонку healthcheck если её нет
      if (!existingColumns.includes('healthcheck')) {
        console.log('Adding healthcheck column...');
        await sequelize.query(`
          ALTER TABLE "Servers" 
          ADD COLUMN "healthcheck" VARCHAR(255)
        `);
      }
      
      console.log('Migration completed successfully');
    },
    down: async () => {
      console.log('Rolling back migration: Remove type and healthcheck columns');
      
      // Удаляем колонки
      await sequelize.query(`ALTER TABLE "Servers" DROP COLUMN IF EXISTS "healthcheck"`);
      await sequelize.query(`ALTER TABLE "Servers" DROP COLUMN IF EXISTS "type"`);
      
      console.log('Migration rollback completed');
    }
  }
];

// Функция для запуска миграций
const runMigrations = async () => {
  try {
    console.log('Starting database migrations...');
    
    for (const migration of migrations) {
      console.log(`\n--- Running migration: ${migration.name} ---`);
      await migration.up();
    }
    
    console.log('\n✅ All migrations completed successfully!');
  } catch (error) {
    console.error('❌ Migration failed:', error);
    throw error;
  }
};

// Функция для отката миграций
const rollbackMigrations = async () => {
  try {
    console.log('Rolling back migrations...');
    
    // Запускаем миграции в обратном порядке
    for (const migration of migrations.reverse()) {
      console.log(`\n--- Rolling back migration: ${migration.name} ---`);
      await migration.down();
    }
    
    console.log('\n✅ All migrations rolled back successfully!');
  } catch (error) {
    console.error('❌ Migration rollback failed:', error);
    throw error;
  }
};

module.exports = {
  runMigrations,
  rollbackMigrations
};
