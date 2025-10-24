// Типы для серверов
export type ServerType = 'Postgres' | 'Redis' | 'Kafka' | 'Astra Linux' | 'Другое';
export type ServerStatus = 'online' | 'offline' | 'unknown';

// Интерфейс сервера
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

// Интерфейс для создания сервера
export interface ICreateServer {
  name: string;
  url: string;
  type: ServerType;
  healthcheck?: string;
}

// Интерфейс для обновления сервера
export interface IUpdateServer {
  name?: string;
  url?: string;
  type?: ServerType;
  healthcheck?: string;
}

// Типы для API ответов
export interface IApiResponse<T = any> {
  success: boolean;
  data?: T | undefined;
  error?: string | undefined;
  message?: string | undefined;
}

// Типы для валидации
export interface IValidationError {
  field: string;
  message: string;
}

// Типы для мониторинга
export interface IMonitorStatus {
  isRunning: boolean;
  checkInterval: number;
  nextCheck: Date | null;
}

// Типы для конфигурации
export interface IDatabaseConfig {
  host: string;
  port: number;
  database: string;
  username: string;
  password: string;
}

// Типы для Express
export interface IRequest extends Express.Request {
  body: any;
  params: any;
  query: any;
}

export interface IResponse extends Express.Response {
  json: (data: any) => IResponse;
  status: (code: number) => IResponse;
}
