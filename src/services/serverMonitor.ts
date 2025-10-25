import { Server } from '../models/Server';
import { IMonitorStatus } from '../types';
import dotenv from 'dotenv';

// Загружаем переменные окружения
dotenv.config();

class ServerMonitor {
  private isRunning: boolean = false;
  private intervalId: NodeJS.Timeout | null = null;
  private checkInterval: number = parseInt(process.env.MONITOR_INTERVAL || '30000'); // 30 секунд по умолчанию
  private timeout: number = parseInt(process.env.MONITOR_TIMEOUT || '10000'); // 10 секунд таймаут

  // Запуск мониторинга
  public start(interval: number = this.checkInterval): void {
    if (this.isRunning) {
      console.log('Server monitoring is already running');
      return;
    }

    this.checkInterval = interval;
    this.isRunning = true;
    
    console.log(`Starting server monitoring (checking every ${this.checkInterval / 1000} seconds)`);
    
    // Первая проверка сразу
    this.checkAllServers();
    
    // Затем по интервалу
    this.intervalId = setInterval(() => {
      this.checkAllServers();
    }, this.checkInterval);
  }

  // Остановка мониторинга
  public stop(): void {
    if (!this.isRunning) {
      console.log('Server monitoring is not running');
      return;
    }

    this.isRunning = false;
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
    
    console.log('Server monitoring stopped');
  }

  // Проверка всех серверов
  private async checkAllServers(): Promise<void> {
    try {
      const servers = await Server.findAll();
      
      if (servers.length === 0) {
        console.log('No servers to monitor');
        return;
      }

      console.log(`Checking ${servers.length} server(s)...`);
      
      // Проверяем серверы параллельно
      const checkPromises = servers.map(server => this.checkServer(server));
      await Promise.allSettled(checkPromises);
      
    } catch (error) {
      console.error('Error checking servers:', error);
    }
  }

  // Проверка одного сервера
  private async checkServer(server: Server): Promise<void> {
    try {
      const startTime = Date.now();
      let status: 'online' | 'offline' = 'offline';
      let responseTime = 0;
      let errorMessage = '';

      // Выбираем метод проверки в зависимости от типа сервера
      switch (server.type) {
        case 'Postgres':
          status = await this.checkPostgresServer(server);
          break;
        case 'Redis':
          status = await this.checkRedisServer(server);
          break;
        case 'Kafka':
          status = await this.checkKafkaServer(server);
          break;
        case 'Astra Linux':
          status = await this.checkAstraLinuxServer(server);
          break;
        case 'Другое':
          status = await this.checkCustomServer(server);
          break;
        default:
          status = 'offline';
          errorMessage = 'Unknown server type';
      }

      responseTime = Date.now() - startTime;
      
      await server.update({ 
        status: status,
        lastChecked: new Date()
      });
      
      if (status === 'online') {
        console.log(`✓ Server: ${server.name} | Type: ${server.type} | Status: ${status} | Time: ${responseTime}ms`);
      } else {
        console.log(`✗ Server: ${server.name} | Type: ${server.type} | Status: ${status} | Error: ${errorMessage || 'Connection failed'} | Time: ${responseTime}ms`);
      }
      
    } catch (error) {
      await server.update({ 
        status: 'offline',
        lastChecked: new Date()
      });
      
      console.log(`✗ Server: ${server.name} | Type: ${server.type} | Error: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  }

  // Проверка PostgreSQL сервера
  private async checkPostgresServer(server: Server): Promise<'online' | 'offline'> {
    try {
      // Парсим URL для PostgreSQL
      const url = new URL(server.url);
      const host = url.hostname;
      const port = parseInt(url.port) || 5432;
      
      // Простая проверка TCP соединения
      const net = await import('net');
      return new Promise((resolve) => {
        const socket = net.createConnection(port, host);
        
        const timeout = setTimeout(() => {
          socket.destroy();
          resolve('offline');
        }, this.timeout);
        
        socket.on('connect', () => {
          clearTimeout(timeout);
          socket.destroy();
          resolve('online');
        });
        
        socket.on('error', () => {
          clearTimeout(timeout);
          resolve('offline');
        });
      });
    } catch {
      return 'offline';
    }
  }

  // Проверка Redis сервера
  private async checkRedisServer(server: Server): Promise<'online' | 'offline'> {
    try {
      const url = new URL(server.url);
      const host = url.hostname;
      const port = parseInt(url.port) || 6379;
      
      const net = await import('net');
      return new Promise((resolve) => {
        const socket = net.createConnection(port, host);
        
        const timeout = setTimeout(() => {
          socket.destroy();
          resolve('offline');
        }, this.timeout);
        
        socket.on('connect', () => {
          clearTimeout(timeout);
          socket.destroy();
          resolve('online');
        });
        
        socket.on('error', () => {
          clearTimeout(timeout);
          resolve('offline');
        });
      });
    } catch {
      return 'offline';
    }
  }

  // Проверка Kafka сервера
  private async checkKafkaServer(server: Server): Promise<'online' | 'offline'> {
    try {
      const url = new URL(server.url);
      const host = url.hostname;
      const port = parseInt(url.port) || 9092;
      
      const net = await import('net');
      return new Promise((resolve) => {
        const socket = net.createConnection(port, host);
        
        const timeout = setTimeout(() => {
          socket.destroy();
          resolve('offline');
        }, this.timeout);
        
        socket.on('connect', () => {
          clearTimeout(timeout);
          socket.destroy();
          resolve('online');
        });
        
        socket.on('error', () => {
          clearTimeout(timeout);
          resolve('offline');
        });
      });
    } catch {
      return 'offline';
    }
  }

  // Проверка Astra Linux сервера
  private async checkAstraLinuxServer(server: Server): Promise<'online' | 'offline'> {
    try {
      const url = new URL(server.url);
      const host = url.hostname;
      const port = parseInt(url.port) || 22; // SSH порт по умолчанию
      
      const net = await import('net');
      return new Promise((resolve) => {
        const socket = net.createConnection(port, host);
        
        const timeout = setTimeout(() => {
          socket.destroy();
          resolve('offline');
        }, this.timeout);
        
        socket.on('connect', () => {
          clearTimeout(timeout);
          socket.destroy();
          resolve('online');
        });
        
        socket.on('error', () => {
          clearTimeout(timeout);
          resolve('offline');
        });
      });
    } catch {
      return 'offline';
    }
  }

  // Проверка кастомного сервера (тип "Другое")
  private async checkCustomServer(server: Server): Promise<'online' | 'offline'> {
    try {
      // Определяем URL для проверки
      let checkUrl = server.url;
      
      // Если есть healthcheck, используем его
      if (server.healthcheck) {
        const baseUrl = server.url.endsWith('/') ? server.url.slice(0, -1) : server.url;
        const healthcheckPath = server.healthcheck.startsWith('/') ? server.healthcheck : `/${server.healthcheck}`;
        checkUrl = `${baseUrl}${healthcheckPath}`;
      }
      
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), this.timeout);
      
      const response = await fetch(checkUrl, {
        method: 'GET',
        signal: controller.signal,
        headers: {
          'User-Agent': 'ServerMonitor/1.0',
          'Accept': 'application/json, text/plain, */*'
        }
      });
      
      clearTimeout(timeoutId);
      
      return response.ok ? 'online' : 'offline';
    } catch {
      return 'offline';
    }
  }

  // Получение статуса мониторинга
  public getStatus(): IMonitorStatus {
    return {
      isRunning: this.isRunning,
      checkInterval: this.checkInterval,
      nextCheck: this.isRunning ? new Date(Date.now() + this.checkInterval) : null
    };
  }

  // Изменение интервала проверки
  public setInterval(interval: number): void {
    this.checkInterval = interval;
    
    if (this.isRunning) {
      this.stop();
      this.start(interval);
    }
  }
}

// Создаем единственный экземпляр
const serverMonitor = new ServerMonitor();

export default serverMonitor;
