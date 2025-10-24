const express = require('express');
const Server = require('../models/Server');
const router = express.Router();

// Получение всех серверов
router.get('/', async (req, res) => {
  try {
    const servers = await Server.findAll({
      order: [['createdAt', 'DESC']]
    });
    res.json(servers);
  } catch (err) {
    console.error('Error fetching servers:', err);
    res.status(500).json({ error: err.message });
  }
});

// Создание нового сервера
router.post('/', async (req, res) => {
  try {
    // Валидация данных
    const { name, url, type, healthcheck } = req.body;
    
    if (!name || !url || !type) {
      return res.status(400).json({ error: 'Name, URL and type are required' });
    }
    
    // Проверка формата URL
    try {
      new URL(url);
    } catch (e) {
      return res.status(400).json({ error: 'Invalid URL format' });
    }
    
    // Проверка типа сервера
    const validTypes = ['Postgres', 'Redis', 'Kafka', 'Astra Linux', 'Другое'];
    if (!validTypes.includes(type)) {
      return res.status(400).json({ error: 'Invalid server type' });
    }
    
    // Если тип "Другое", healthcheck обязателен
    if (type === 'Другое' && !healthcheck) {
      return res.status(400).json({ error: 'Healthcheck is required for "Другое" type' });
    }
    
    // Проверка на дублирование
    const existingServer = await Server.findOne({ where: { url } });
    if (existingServer) {
      return res.status(409).json({ error: 'Server with this URL already exists' });
    }
    
    const server = await Server.create({ name, url, type, healthcheck });
    res.status(201).json(server);
  } catch (err) {
    console.error('Error creating server:', err);
    res.status(400).json({ error: err.message });
  }
});

// Получение сервера по ID
router.get('/:id', async (req, res) => {
  try {
    const server = await Server.findByPk(req.params.id);
    if (!server) {
      return res.status(404).json({ error: 'Server not found' });
    }
    res.json(server);
  } catch (err) {
    console.error('Error fetching server:', err);
    res.status(500).json({ error: err.message });
  }
});

// Обновление сервера
router.put('/:id', async (req, res) => {
  try {
    const { name, url, type, healthcheck } = req.body;
    
    if (!name || !url || !type) {
      return res.status(400).json({ error: 'Name, URL and type are required' });
    }
    
    // Проверка формата URL
    try {
      new URL(url);
    } catch (e) {
      return res.status(400).json({ error: 'Invalid URL format' });
    }
    
    // Проверка типа сервера
    const validTypes = ['Postgres', 'Redis', 'Kafka', 'Astra Linux', 'Другое'];
    if (!validTypes.includes(type)) {
      return res.status(400).json({ error: 'Invalid server type' });
    }
    
    // Если тип "Другое", healthcheck обязателен
    if (type === 'Другое' && !healthcheck) {
      return res.status(400).json({ error: 'Healthcheck is required for "Другое" type' });
    }
    
    const server = await Server.findByPk(req.params.id);
    if (!server) {
      return res.status(404).json({ error: 'Server not found' });
    }
    
    await server.update({ name, url, type, healthcheck });
    res.json(server);
  } catch (err) {
    console.error('Error updating server:', err);
    res.status(400).json({ error: err.message });
  }
});

// Удаление сервера
router.delete('/:id', async (req, res) => {
  try {
    const server = await Server.findByPk(req.params.id);
    if (!server) {
      return res.status(404).json({ error: 'Server not found' });
    }
    
    await server.destroy();
    res.status(204).send();
  } catch (err) {
    console.error('Error deleting server:', err);
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
