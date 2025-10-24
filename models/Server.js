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
