"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const Server_1 = require("../models/Server");
class ServerMonitor {
    constructor() {
        this.isRunning = false;
        this.intervalId = null;
        this.checkInterval = 30000;
    }
    start(interval = this.checkInterval) {
        if (this.isRunning) {
            console.log('Server monitoring is already running');
            return;
        }
        this.checkInterval = interval;
        this.isRunning = true;
        console.log(`Starting server monitoring (checking every ${this.checkInterval / 1000} seconds)`);
        this.checkAllServers();
        this.intervalId = setInterval(() => {
            this.checkAllServers();
        }, this.checkInterval);
    }
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
    async checkAllServers() {
        try {
            const servers = await Server_1.Server.findAll();
            if (servers.length === 0) {
                console.log('No servers to monitor');
                return;
            }
            console.log(`Checking ${servers.length} server(s)...`);
            const checkPromises = servers.map(server => this.checkServer(server));
            await Promise.allSettled(checkPromises);
        }
        catch (error) {
            console.error('Error checking servers:', error);
        }
    }
    async checkServer(server) {
        try {
            const startTime = Date.now();
            let checkUrl = server.url;
            if (server.healthcheck) {
                const baseUrl = server.url.endsWith('/') ? server.url.slice(0, -1) : server.url;
                const healthcheckPath = server.healthcheck.startsWith('/') ? server.healthcheck : `/${server.healthcheck}`;
                checkUrl = `${baseUrl}${healthcheckPath}`;
            }
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 10000);
            const response = await fetch(checkUrl, {
                method: 'GET',
                signal: controller.signal,
                headers: {
                    'User-Agent': 'ServerMonitor/1.0',
                    'Accept': 'application/json, text/plain, */*'
                }
            });
            clearTimeout(timeoutId);
            const responseTime = Date.now() - startTime;
            const status = response.ok ? 'online' : 'offline';
            await server.update({
                status: status,
                lastChecked: new Date()
            });
            const healthcheckInfo = server.healthcheck ? ` (healthcheck: ${server.healthcheck})` : '';
            console.log(`✓ Server: ${server.name} | Type: ${server.type} | Status: ${status} | Response: ${response.status} | Time: ${responseTime}ms${healthcheckInfo}`);
        }
        catch (error) {
            await server.update({
                status: 'offline',
                lastChecked: new Date()
            });
            const healthcheckInfo = server.healthcheck ? ` (healthcheck: ${server.healthcheck})` : '';
            console.log(`✗ Server: ${server.name} | Type: ${server.type} | Error: ${error instanceof Error ? error.message : 'Unknown error'}${healthcheckInfo}`);
        }
    }
    getStatus() {
        return {
            isRunning: this.isRunning,
            checkInterval: this.checkInterval,
            nextCheck: this.isRunning ? new Date(Date.now() + this.checkInterval) : null
        };
    }
    setInterval(interval) {
        this.checkInterval = interval;
        if (this.isRunning) {
            this.stop();
            this.start(interval);
        }
    }
}
const serverMonitor = new ServerMonitor();
exports.default = serverMonitor;
//# sourceMappingURL=serverMonitor.js.map