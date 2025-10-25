"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const Server_1 = require("../models/Server");
const dotenv_1 = __importDefault(require("dotenv"));
dotenv_1.default.config();
class ServerMonitor {
    constructor() {
        this.isRunning = false;
        this.intervalId = null;
        this.checkInterval = parseInt(process.env.MONITOR_INTERVAL || '30000');
        this.timeout = parseInt(process.env.MONITOR_TIMEOUT || '10000');
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
            let status = 'offline';
            let responseTime = 0;
            let errorMessage = '';
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
            }
            else {
                console.log(`✗ Server: ${server.name} | Type: ${server.type} | Status: ${status} | Error: ${errorMessage || 'Connection failed'} | Time: ${responseTime}ms`);
            }
        }
        catch (error) {
            await server.update({
                status: 'offline',
                lastChecked: new Date()
            });
            console.log(`✗ Server: ${server.name} | Type: ${server.type} | Error: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }
    async checkPostgresServer(server) {
        try {
            const url = new URL(server.url);
            const host = url.hostname;
            const port = parseInt(url.port) || 5432;
            const net = await Promise.resolve().then(() => __importStar(require('net')));
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
        }
        catch {
            return 'offline';
        }
    }
    async checkRedisServer(server) {
        try {
            const url = new URL(server.url);
            const host = url.hostname;
            const port = parseInt(url.port) || 6379;
            const net = await Promise.resolve().then(() => __importStar(require('net')));
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
        }
        catch {
            return 'offline';
        }
    }
    async checkKafkaServer(server) {
        try {
            const url = new URL(server.url);
            const host = url.hostname;
            const port = parseInt(url.port) || 9092;
            const net = await Promise.resolve().then(() => __importStar(require('net')));
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
        }
        catch {
            return 'offline';
        }
    }
    async checkAstraLinuxServer(server) {
        try {
            const url = new URL(server.url);
            const host = url.hostname;
            const port = parseInt(url.port) || 22;
            const net = await Promise.resolve().then(() => __importStar(require('net')));
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
        }
        catch {
            return 'offline';
        }
    }
    async checkCustomServer(server) {
        try {
            let checkUrl = server.url;
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
        }
        catch {
            return 'offline';
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