"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const database_1 = require("./config/database");
const serverMonitor_1 = __importDefault(require("./services/serverMonitor"));
const servers_1 = __importDefault(require("./routes/servers"));
const dotenv_1 = __importDefault(require("dotenv"));
dotenv_1.default.config();
const app = (0, express_1.default)();
const PORT = process.env.PORT || 3001;
app.use(express_1.default.json());
app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', process.env.CORS_ORIGIN || '*');
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
    if (req.method === 'OPTIONS') {
        res.sendStatus(200);
    }
    else {
        next();
    }
});
app.use('/api/servers', servers_1.default);
app.get('/', (_req, res) => {
    res.json({
        message: 'Server Dashboard API',
        version: '1.0.0',
        endpoints: {
            servers: '/api/servers'
        }
    });
});
app.use((err, _req, res, _next) => {
    console.error('Unhandled error:', err);
    res.status(500).json({
        success: false,
        error: 'Internal server error',
        message: 'An unexpected error occurred'
    });
});
const startServer = async () => {
    try {
        await (0, database_1.connectDB)();
        await (0, database_1.syncDatabase)();
        serverMonitor_1.default.start();
        app.listen(PORT, () => {
            console.log(`🚀 Server is running on http://localhost:${PORT}`);
            console.log(`📊 Server monitoring started`);
        });
    }
    catch (error) {
        console.error('Failed to start server:', error);
        process.exit(1);
    }
};
process.on('SIGINT', () => {
    console.log('\n🛑 Shutting down server...');
    serverMonitor_1.default.stop();
    process.exit(0);
});
process.on('SIGTERM', () => {
    console.log('\n🛑 Shutting down server...');
    serverMonitor_1.default.stop();
    process.exit(0);
});
startServer();
//# sourceMappingURL=server.js.map