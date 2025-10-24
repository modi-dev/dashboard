import { DataTypes, Model, Optional } from 'sequelize';
import { sequelize } from '../config/database';
import { IServer, ServerType, ServerStatus } from '../types';

// Интерфейс для атрибутов модели
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

// Интерфейс для создания сервера (без id, createdAt, updatedAt)
interface ServerCreationAttributes extends Optional<ServerAttributes, 'id' | 'createdAt' | 'updatedAt' | 'status'> {
  name: string;
  url: string;
  type: ServerType;
  healthcheck?: string;
  status?: ServerStatus;
  lastChecked?: Date;
}

// Класс модели сервера
class Server extends Model<ServerAttributes, ServerCreationAttributes> implements ServerAttributes {
  public id!: number;
  public name!: string;
  public url!: string;
  public type!: ServerType;
  public healthcheck?: string;
  public status!: ServerStatus;
  public lastChecked?: Date;
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;

  // Методы для валидации
  public validateUrl(): boolean {
    try {
      new URL(this.url);
      return true;
    } catch {
      return false;
    }
  }

  public validateHealthcheck(): boolean {
    if (!this.healthcheck) return true;
    return this.healthcheck.length <= 500;
  }

  public isHealthcheckRequired(): boolean {
    return this.type === 'Другое';
  }
}

// Определение модели
Server.init(
  {
    id: {
      type: DataTypes.INTEGER,
      autoIncrement: true,
      primaryKey: true
    },
    name: {
      type: DataTypes.STRING,
      allowNull: false,
      validate: {
        notEmpty: true,
        len: [1, 255]
      }
    },
    url: {
      type: DataTypes.STRING,
      allowNull: false,
      validate: {
        notEmpty: true,
        isUrl: true
      }
    },
    type: {
      type: DataTypes.ENUM('Postgres', 'Redis', 'Kafka', 'Astra Linux', 'Другое'),
      allowNull: false,
      defaultValue: 'Другое'
    },
    healthcheck: {
      type: DataTypes.STRING,
      allowNull: true,
      validate: {
        len: [0, 500]
      }
    },
    status: {
      type: DataTypes.ENUM('online', 'offline', 'unknown'),
      defaultValue: 'unknown'
    },
    lastChecked: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW
    },
    createdAt: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    },
    updatedAt: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    }
  },
  {
    sequelize,
    modelName: 'Server',
    tableName: 'Servers',
    timestamps: true,
    createdAt: 'createdAt',
    updatedAt: 'updatedAt'
  }
);

export { Server };
export default Server;
