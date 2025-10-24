const Server = require('../models/Server');

class ServerMonitor {
  constructor() {
    this.isRunning = false;
    this.intervalId = null;
    this.checkInterval = 30000; // 30 секунд по умолчанию
  }

  // Запуск мониторинга
  start(interval = this.checkInterval) {
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
  stop() {
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
  async checkAllServers() {
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
  async checkServer(server) {
    try {
      const startTime = Date.now();
      const response = await fetch(server.url, {
        method: 'GET',
        timeout: 10000, // 10 секунд таймаут
        headers: {
          'User-Agent': 'ServerMonitor/1.0'
        }
      });
      
      const responseTime = Date.now() - startTime;
      const status = response.ok ? 'online' : 'offline';
      
      await server.update({ 
        status: status,
        lastChecked: new Date()
      });
      
      console.log(`✓ Server: ${server.name} | Status: ${status} | Response: ${response.status} | Time: ${responseTime}ms`);
      
    } catch (error) {
      await server.update({ 
        status: 'offline',
        lastChecked: new Date()
      });
      
      console.log(`✗ Server: ${server.name} | Error: ${error.message}`);
    }
  }

  // Получение статуса мониторинга
  getStatus() {
    return {
      isRunning: this.isRunning,
      checkInterval: this.checkInterval,
      nextCheck: this.isRunning ? new Date(Date.now() + this.checkInterval) : null
    };
  }

  // Изменение интервала проверки
  setInterval(interval) {
    this.checkInterval = interval;
    
    if (this.isRunning) {
      this.stop();
      this.start(interval);
    }
  }
}

// Создаем единственный экземпляр
const serverMonitor = new ServerMonitor();

module.exports = serverMonitor;
