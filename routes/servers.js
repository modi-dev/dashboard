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
    const { name, url } = req.body;
    
    if (!name || !url) {
      return res.status(400).json({ error: 'Name and URL are required' });
    }
    
    // Проверка формата URL
    try {
      new URL(url);
    } catch (e) {
      return res.status(400).json({ error: 'Invalid URL format' });
    }
    
    // Проверка на дублирование
    const existingServer = await Server.findOne({ where: { url } });
    if (existingServer) {
      return res.status(409).json({ error: 'Server with this URL already exists' });
    }
    
    const server = await Server.create({ name, url });
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
    const { name, url } = req.body;
    
    if (!name || !url) {
      return res.status(400).json({ error: 'Name and URL are required' });
    }
    
    // Проверка формата URL
    try {
      new URL(url);
    } catch (e) {
      return res.status(400).json({ error: 'Invalid URL format' });
    }
    
    const server = await Server.findByPk(req.params.id);
    if (!server) {
      return res.status(404).json({ error: 'Server not found' });
    }
    
    await server.update({ name, url });
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
