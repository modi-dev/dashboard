import { runMigrations, rollbackMigrations } from './migrate';

const args = process.argv.slice(2);
const shouldRollback = args.includes('--rollback') || args.includes('-r');

(async () => {
  try {
    if (shouldRollback) {
      console.log('Running rollback...');
      await rollbackMigrations();
    } else {
      console.log('Running migrations...');
      await runMigrations();
    }
    process.exit(0);
  } catch (err) {
    console.error('Migration error:', err);
    process.exit(1);
  }
})();