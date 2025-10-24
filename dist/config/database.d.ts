import { Sequelize } from 'sequelize';
declare const sequelize: Sequelize;
export declare const connectDB: () => Promise<Sequelize>;
export declare const syncDatabase: () => Promise<void>;
export { sequelize };
export default sequelize;
//# sourceMappingURL=database.d.ts.map