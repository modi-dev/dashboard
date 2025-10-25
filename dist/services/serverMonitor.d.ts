import { IMonitorStatus } from '../types';
declare class ServerMonitor {
    private isRunning;
    private intervalId;
    private checkInterval;
    private timeout;
    start(interval?: number): void;
    stop(): void;
    private checkAllServers;
    private checkServer;
    private checkPostgresServer;
    private checkRedisServer;
    private checkKafkaServer;
    private checkAstraLinuxServer;
    private checkCustomServer;
    getStatus(): IMonitorStatus;
    setInterval(interval: number): void;
}
declare const serverMonitor: ServerMonitor;
export default serverMonitor;
//# sourceMappingURL=serverMonitor.d.ts.map