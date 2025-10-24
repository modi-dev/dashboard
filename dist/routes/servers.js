"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const Server_1 = require("../models/Server");
const router = (0, express_1.Router)();
const validTypes = ['Postgres', 'Redis', 'Kafka', 'Astra Linux', 'Другое'];
const createResponse = (success, data, error, message) => ({
    success,
    data,
    error,
    message
});
router.get('/', async (_req, res) => {
    try {
        const servers = await Server_1.Server.findAll({
            order: [['createdAt', 'DESC']]
        });
        res.json(createResponse(true, servers));
    }
    catch (err) {
        console.error('Error fetching servers:', err);
        res.status(500).json(createResponse(false, undefined, 'Internal server error'));
    }
});
router.post('/', async (req, res) => {
    try {
        const { name, url, type, healthcheck } = req.body;
        if (!name || !url || !type) {
            res.status(400).json(createResponse(false, undefined, 'Name, URL and type are required'));
            return;
        }
        try {
            new URL(url);
        }
        catch (e) {
            res.status(400).json(createResponse(false, undefined, 'Invalid URL format'));
            return;
        }
        if (!validTypes.includes(type)) {
            res.status(400).json(createResponse(false, undefined, 'Invalid server type'));
            return;
        }
        if (type === 'Другое' && !healthcheck) {
            res.status(400).json(createResponse(false, undefined, 'Healthcheck is required for "Другое" type'));
            return;
        }
        const existingServer = await Server_1.Server.findOne({ where: { url } });
        if (existingServer) {
            res.status(409).json(createResponse(false, undefined, 'Server with this URL already exists'));
            return;
        }
        const server = await Server_1.Server.create({
            name,
            url,
            type,
            healthcheck: healthcheck || undefined
        });
        res.status(201).json(createResponse(true, server, undefined, 'Server created successfully'));
    }
    catch (err) {
        console.error('Error creating server:', err);
        res.status(400).json(createResponse(false, undefined, 'Failed to create server'));
    }
});
router.get('/:id', async (req, res) => {
    try {
        const id = parseInt(req.params['id']);
        if (isNaN(id)) {
            res.status(400).json(createResponse(false, undefined, 'Invalid server ID'));
            return;
        }
        const server = await Server_1.Server.findByPk(id);
        if (!server) {
            res.status(404).json(createResponse(false, undefined, 'Server not found'));
            return;
        }
        res.json(createResponse(true, server));
    }
    catch (err) {
        console.error('Error fetching server:', err);
        res.status(500).json(createResponse(false, undefined, 'Internal server error'));
    }
});
router.put('/:id', async (req, res) => {
    try {
        const id = parseInt(req.params['id']);
        const { name, url, type, healthcheck } = req.body;
        if (isNaN(id)) {
            res.status(400).json(createResponse(false, undefined, 'Invalid server ID'));
            return;
        }
        if (!name || !url || !type) {
            res.status(400).json(createResponse(false, undefined, 'Name, URL and type are required'));
            return;
        }
        try {
            new URL(url);
        }
        catch (e) {
            res.status(400).json(createResponse(false, undefined, 'Invalid URL format'));
            return;
        }
        if (!validTypes.includes(type)) {
            res.status(400).json(createResponse(false, undefined, 'Invalid server type'));
            return;
        }
        if (type === 'Другое' && !healthcheck) {
            res.status(400).json(createResponse(false, undefined, 'Healthcheck is required for "Другое" type'));
            return;
        }
        const server = await Server_1.Server.findByPk(id);
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
    }
    catch (err) {
        console.error('Error updating server:', err);
        res.status(400).json(createResponse(false, undefined, 'Failed to update server'));
    }
});
router.delete('/:id', async (req, res) => {
    try {
        const id = parseInt(req.params['id']);
        if (isNaN(id)) {
            res.status(400).json(createResponse(false, undefined, 'Invalid server ID'));
            return;
        }
        const server = await Server_1.Server.findByPk(id);
        if (!server) {
            res.status(404).json(createResponse(false, undefined, 'Server not found'));
            return;
        }
        await server.destroy();
        res.status(204).send();
    }
    catch (err) {
        console.error('Error deleting server:', err);
        res.status(500).json(createResponse(false, undefined, 'Internal server error'));
    }
});
exports.default = router;
//# sourceMappingURL=servers.js.map