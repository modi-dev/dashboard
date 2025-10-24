export type ServerType = 'Postgres' | 'Redis' | 'Kafka' | 'Astra Linux' | 'Другое';
export type ServerStatus = 'online' | 'offline' | 'unknown';
export interface IServer {
    id?: number;
    name: string;
    url: string;
    type: ServerType;
    healthcheck?: string;
    status: ServerStatus;
    lastChecked?: Date;
    createdAt?: Date;
    updatedAt?: Date;
}
export interface ICreateServer {
    name: string;
    url: string;
    type: ServerType;
    healthcheck?: string;
}
export interface IUpdateServer {
    name?: string;
    url?: string;
    type?: ServerType;
    healthcheck?: string;
}
export interface IApiResponse<T = any> {
    success: boolean;
    data?: T | undefined;
    error?: string | undefined;
    message?: string | undefined;
}
export interface IValidationError {
    field: string;
    message: string;
}
export interface IMonitorStatus {
    isRunning: boolean;
    checkInterval: number;
    nextCheck: Date | null;
}
export interface IDatabaseConfig {
    host: string;
    port: number;
    database: string;
    username: string;
    password: string;
}
export interface IRequest extends Express.Request {
    body: any;
    params: any;
    query: any;
}
export interface IResponse extends Express.Response {
    json: (data: any) => IResponse;
    status: (code: number) => IResponse;
}
//# sourceMappingURL=index.d.ts.map