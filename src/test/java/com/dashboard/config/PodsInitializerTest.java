package com.dashboard.config;

import com.dashboard.service.KubernetesPodsSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Тесты для PodsInitializer
 */
@ExtendWith(MockitoExtension.class)
class PodsInitializerTest {
    
    @Mock
    private KubernetesPodsSyncService podsSyncService;
    
    @InjectMocks
    private PodsInitializer podsInitializer;
    
    @BeforeEach
    void setUp() {
        // Setup is done by Mockito annotations
    }
    
    @Test
    void testRun_Success() throws Exception {
        // Given
        when(podsSyncService.syncPods()).thenReturn(5);
        
        // When
        podsInitializer.run();
        
        // Then
        verify(podsSyncService, times(1)).syncPods();
    }
    
    @Test
    void testRun_WithArguments() throws Exception {
        // Given
        when(podsSyncService.syncPods()).thenReturn(10);
        
        // When
        podsInitializer.run("arg1", "arg2");
        
        // Then
        verify(podsSyncService, times(1)).syncPods();
    }
    
    @Test
    void testRun_WhenExceptionThrown_ShouldNotRethrow() throws Exception {
        // Given
        when(podsSyncService.syncPods()).thenThrow(new RuntimeException("Test exception"));
        
        // When - should not throw exception
        podsInitializer.run();
        
        // Then
        verify(podsSyncService, times(1)).syncPods();
    }
    
    @Test
    void testRun_WhenSyncReturnsZero() throws Exception {
        // Given
        when(podsSyncService.syncPods()).thenReturn(0);
        
        // When
        podsInitializer.run();
        
        // Then
        verify(podsSyncService, times(1)).syncPods();
    }
    
    @Test
    void testRun_WhenSyncThrowsIllegalStateException() throws Exception {
        // Given
        when(podsSyncService.syncPods()).thenThrow(new IllegalStateException("Service not ready"));
        
        // When - should not throw exception
        podsInitializer.run();
        
        // Then
        verify(podsSyncService, times(1)).syncPods();
    }
}
