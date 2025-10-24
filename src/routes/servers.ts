import { Router, Request, Response } from 'express';
import { Server } from '../models/Server';
import { ICreateServer, IUpdateServer, IApiResponse, ServerType } from '../types';

const router = Router();

// Валидация типов серверов
const validTypes: ServerType[] = ['Postgres', 'Redis', 'Kafka', 'Astra Linux', 'Другое'];

// Вспомогательная функция для создания ответа
const createResponse = <T>(success: boolean, data?: T, error?: string, message?: string): IApiResponse<T> => ({
  success,
  data,
  error,
  message
});

// Получение всех серверов
router.get('/', async (_req: Request, res: Response): Promise<void> => {
  try {
    const servers = await Server.findAll({
      order: [['createdAt', 'DESC']]
    });
    
    res.json(createResponse(true, servers));
  } catch (err) {
    console.error('Error fetching servers:', err);
    res.status(500).json(createResponse(false, undefined, 'Internal server error'));
  }
});

// Создание нового сервера
router.post('/', async (req: Request, res: Response): Promise<void> => {
  try {
    const { name, url, type, healthcheck }: ICreateServer = req.body;
    
    // Валидация обязательных полей
    if (!name || !url || !type) {
      res.status(400).json(createResponse(false, undefined, 'Name, URL and type are required'));
      return;
    }
    
    // Проверка формата URL
    try {
      new URL(url);
    } catch (e) {
      res.status(400).json(createResponse(false, undefined, 'Invalid URL format'));
      return;
    }
    
    // Проверка типа сервера
    if (!validTypes.includes(type)) {
      res.status(400).json(createResponse(false, undefined, 'Invalid server type'));
      return;
    }
    
    // Если тип "Другое", healthcheck обязателен
    if (type === 'Другое' && !healthcheck) {
      res.status(400).json(createResponse(false, undefined, 'Healthcheck is required for "Другое" type'));
      return;
    }
    
    // Проверка на дублирование
    const existingServer = await Server.findOne({ where: { url } });
    if (existingServer) {
      res.status(409).json(createResponse(false, undefined, 'Server with this URL already exists'));
      return;
    }
    
    const server = await Server.create({ 
      name, 
      url, 
      type, 
      healthcheck: healthcheck || undefined 
    });
    res.status(201).json(createResponse(true, server, undefined, 'Server created successfully'));
  } catch (err) {
    console.error('Error creating server:', err);
    res.status(400).json(createResponse(false, undefined, 'Failed to create server'));
  }
});

// Получение сервера по ID
router.get('/:id', async (req: Request, res: Response): Promise<void> => {
  try {
    const id = parseInt(req.params['id']);
    
    if (isNaN(id)) {
      res.status(400).json(createResponse(false, undefined, 'Invalid server ID'));
      return;
    }
    
    const server = await Server.findByPk(id);
    if (!server) {
      res.status(404).json(createResponse(false, undefined, 'Server not found'));
      return;
    }
    
    res.json(createResponse(true, server));
  } catch (err) {
    console.error('Error fetching server:', err);
    res.status(500).json(createResponse(false, undefined, 'Internal server error'));
  }
});

// Обновление сервера
router.put('/:id', async (req: Request, res: Response): Promise<void> => {
  try {
    const id = parseInt(req.params['id']);
    const { name, url, type, healthcheck }: IUpdateServer = req.body;
    
    if (isNaN(id)) {
      res.status(400).json(createResponse(false, undefined, 'Invalid server ID'));
      return;
    }
    
    if (!name || !url || !type) {
      res.status(400).json(createResponse(false, undefined, 'Name, URL and type are required'));
      return;
    }
    
    // Проверка формата URL
    try {
      new URL(url);
    } catch (e) {
      res.status(400).json(createResponse(false, undefined, 'Invalid URL format'));
      return;
    }
    
    // Проверка типа сервера
    if (!validTypes.includes(type)) {
      res.status(400).json(createResponse(false, undefined, 'Invalid server type'));
      return;
    }
    
    // Если тип "Другое", healthcheck обязателен
    if (type === 'Другое' && !healthcheck) {
      res.status(400).json(createResponse(false, undefined, 'Healthcheck is required for "Другое" type'));
      return;
    }
    
    const server = await Server.findByPk(id);
    if (!server) {
      res.status(404).json(createResponse(false, undefined, 'Server not found'));
      return;
    }
    
    await server.update({ 
      name, 
      url, 
      type, 
      healthcheck: healthcheck || undefined 
    });
    res.json(createResponse(true, server, undefined, 'Server updated successfully'));
  } catch (err) {
    console.error('Error updating server:', err);
    res.status(400).json(createResponse(false, undefined, 'Failed to update server'));
  }
});

// Удаление сервера
router.delete('/:id', async (req: Request, res: Response): Promise<void> => {
  try {
    const id = parseInt(req.params['id']);
    
    if (isNaN(id)) {
      res.status(400).json(createResponse(false, undefined, 'Invalid server ID'));
      return;
    }
    
    const server = await Server.findByPk(id);
    if (!server) {
      res.status(404).json(createResponse(false, undefined, 'Server not found'));
      return;
    }
    
    await server.destroy();
    res.status(204).send();
  } catch (err) {
    console.error('Error deleting server:', err);
    res.status(500).json(createResponse(false, undefined, 'Internal server error'));
  }
});

export default router;
