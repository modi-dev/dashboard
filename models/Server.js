const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

// Определение модели Server
const Server = sequelize.define('Server', {
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
  }
}, {
  timestamps: true,
  createdAt: 'createdAt',
  updatedAt: 'updatedAt'
});

module.exports = Server;
