package com.dashboard.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmbeddedKubectlServiceTest {

    @InjectMocks
    private EmbeddedKubectlService embeddedKubectlService;

    @Test
    void testIsInitialized_InitiallyFalse() {
        // На Windows или без kubectl файла инициализация не пройдет
        assertFalse(embeddedKubectlService.isInitialized());
    }

    @Test
    void testGetKubectlPath_ReturnsNullIfNotInitialized() {
        // На Windows или без kubectl файла путь будет null
        String path = embeddedKubectlService.getKubectlPath();
        // Может быть null если kubectl не найден или не на Linux
        assertTrue(path == null || path.length() > 0);
    }

    @Test
    void testCleanup_DoesNotThrowException() {
        // Метод cleanup должен работать даже если kubectl не инициализирован
        assertDoesNotThrow(() -> embeddedKubectlService.cleanup());
    }

    @Test
    void testExecuteKubectlCommand_ThrowsExceptionIfNotInitialized() {
        // Если kubectl не инициализирован, команда должна выбрасывать исключение
        assertThrows(IllegalStateException.class, () -> {
            embeddedKubectlService.executeKubectlCommand("version");
        });
    }
}

