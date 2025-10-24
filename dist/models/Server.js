"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Server = void 0;
const sequelize_1 = require("sequelize");
const database_1 = require("../config/database");
class Server extends sequelize_1.Model {
    validateUrl() {
        try {
            new URL(this.url);
            return true;
        }
        catch {
            return false;
        }
    }
    validateHealthcheck() {
        if (!this.healthcheck)
            return true;
        return this.healthcheck.length <= 500;
    }
    isHealthcheckRequired() {
        return this.type === 'Другое';
    }
}
exports.Server = Server;
Server.init({
    id: {
        type: sequelize_1.DataTypes.INTEGER,
        autoIncrement: true,
        primaryKey: true
    },
    name: {
        type: sequelize_1.DataTypes.STRING,
        allowNull: false,
        validate: {
            notEmpty: true,
            len: [1, 255]
        }
    },
    url: {
        type: sequelize_1.DataTypes.STRING,
        allowNull: false,
        validate: {
            notEmpty: true,
            isUrl: true
        }
    },
    type: {
        type: sequelize_1.DataTypes.ENUM('Postgres', 'Redis', 'Kafka', 'Astra Linux', 'Другое'),
        allowNull: false,
        defaultValue: 'Другое'
    },
    healthcheck: {
        type: sequelize_1.DataTypes.STRING,
        allowNull: true,
        validate: {
            len: [0, 500]
        }
    },
    status: {
        type: sequelize_1.DataTypes.ENUM('online', 'offline', 'unknown'),
        defaultValue: 'unknown'
    },
    lastChecked: {
        type: sequelize_1.DataTypes.DATE,
        defaultValue: sequelize_1.DataTypes.NOW
    },
    createdAt: {
        type: sequelize_1.DataTypes.DATE,
        allowNull: false,
        defaultValue: sequelize_1.DataTypes.NOW
    },
    updatedAt: {
        type: sequelize_1.DataTypes.DATE,
        allowNull: false,
        defaultValue: sequelize_1.DataTypes.NOW
    }
}, {
    sequelize: database_1.sequelize,
    modelName: 'Server',
    tableName: 'Servers',
    timestamps: true,
    createdAt: 'createdAt',
    updatedAt: 'updatedAt'
});
exports.default = Server;
//# sourceMappingURL=Server.js.map