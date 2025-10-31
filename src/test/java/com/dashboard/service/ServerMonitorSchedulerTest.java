package com.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerMonitorSchedulerTest {

    @Mock
    private ServerMonitorService serverMonitorService;

    private ServerMonitorScheduler serverMonitorScheduler;

    @BeforeEach
    void setUp() {
        // Создаем экземпляр с моком сервиса и значением интервала по умолчанию (5 минут)
        serverMonitorScheduler = new ServerMonitorScheduler(serverMonitorService, 5.0);
    }

    @Test
    void testCheckAllServers_Success() {
        doNothing().when(serverMonitorService).checkAllServers();

        serverMonitorScheduler.checkAllServers();

        verify(serverMonitorService, times(1)).checkAllServers();
    }

    @Test
    void testCheckAllServers_ExceptionHandling() {
        doThrow(new RuntimeException("Monitor error")).when(serverMonitorService).checkAllServers();

        // Метод не должен бросать исключение, а только логировать
        serverMonitorScheduler.checkAllServers();

        verify(serverMonitorService, times(1)).checkAllServers();
    }
}

