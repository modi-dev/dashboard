"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.rollbackMigrations = exports.runMigrations = void 0;
const database_1 = require("../config/database");
const migrations = [
    {
        name: '001-add-type-and-healthcheck',
        up: async () => {
            console.log('Running migration: Add type and healthcheck columns');
            const [results] = await database_1.sequelize.query(`
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'Servers' 
        AND column_name IN ('type', 'healthcheck')
      `);
            const existingColumns = results.map(row => row.column_name);
            if (!existingColumns.includes('type')) {
                console.log('Adding type column...');
                await database_1.sequelize.query(`
          ALTER TABLE "Servers" 
          ADD COLUMN "type" VARCHAR(255) DEFAULT 'Другое' NOT NULL
        `);
            }
            if (!existingColumns.includes('healthcheck')) {
                console.log('Adding healthcheck column...');
                await database_1.sequelize.query(`
          ALTER TABLE "Servers" 
          ADD COLUMN "healthcheck" VARCHAR(255)
        `);
            }
            console.log('Migration completed successfully');
        },
        down: async () => {
            console.log('Rolling back migration: Remove type and healthcheck columns');
            await database_1.sequelize.query(`ALTER TABLE "Servers" DROP COLUMN IF EXISTS "healthcheck"`);
            await database_1.sequelize.query(`ALTER TABLE "Servers" DROP COLUMN IF EXISTS "type"`);
            console.log('Migration rollback completed');
        }
    }
];
const runMigrations = async () => {
    try {
        console.log('Starting database migrations...');
        for (const migration of migrations) {
            console.log(`\n--- Running migration: ${migration.name} ---`);
            await migration.up();
        }
        console.log('\n✅ All migrations completed successfully!');
    }
    catch (error) {
        console.error('❌ Migration failed:', error);
        throw error;
    }
};
exports.runMigrations = runMigrations;
const rollbackMigrations = async () => {
    try {
        console.log('Rolling back migrations...');
        for (const migration of migrations.reverse()) {
            console.log(`\n--- Rolling back migration: ${migration.name} ---`);
            await migration.down();
        }
        console.log('\n✅ All migrations rolled back successfully!');
    }
    catch (error) {
        console.error('❌ Migration rollback failed:', error);
        throw error;
    }
};
exports.rollbackMigrations = rollbackMigrations;
//# sourceMappingURL=migrate.js.map