import { Model, Optional } from 'sequelize';
import { IServer, ServerType, ServerStatus } from '../types';
interface ServerAttributes extends IServer {
    id: number;
    name: string;
    url: string;
    type: ServerType;
    healthcheck?: string;
    status: ServerStatus;
    lastChecked?: Date;
    createdAt: Date;
    updatedAt: Date;
}
interface ServerCreationAttributes extends Optional<ServerAttributes, 'id' | 'createdAt' | 'updatedAt' | 'status'> {
    name: string;
    url: string;
    type: ServerType;
    healthcheck?: string;
    status?: ServerStatus;
    lastChecked?: Date;
}
declare class Server extends Model<ServerAttributes, ServerCreationAttributes> implements ServerAttributes {
    id: number;
    name: string;
    url: string;
    type: ServerType;
    healthcheck?: string;
    status: ServerStatus;
    lastChecked?: Date;
    readonly createdAt: Date;
    readonly updatedAt: Date;
    validateUrl(): boolean;
    validateHealthcheck(): boolean;
    isHealthcheckRequired(): boolean;
}
export { Server };
export default Server;
//# sourceMappingURL=Server.d.ts.map