const { DataTypes } = require('sequelize');

module.exports = {
  up: async (queryInterface, Sequelize) => {
    // Добавляем колонку type
    await queryInterface.addColumn('Servers', 'type', {
      type: DataTypes.ENUM('Postgres', 'Redis', 'Kafka', 'Astra Linux', 'Другое'),
      allowNull: false,
      defaultValue: 'Другое'
    });

    // Добавляем колонку healthcheck
    await queryInterface.addColumn('Servers', 'healthcheck', {
      type: DataTypes.STRING,
      allowNull: true
    });
  },

  down: async (queryInterface, Sequelize) => {
    // Удаляем колонки при откате
    await queryInterface.removeColumn('Servers', 'healthcheck');
    await queryInterface.removeColumn('Servers', 'type');
  }
};
