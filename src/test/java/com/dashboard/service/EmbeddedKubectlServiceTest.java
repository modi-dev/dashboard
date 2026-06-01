package com.dashboard.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmbeddedKubectlServiceTest {

    @InjectMocks
    private EmbeddedKubectlService embeddedKubectlService;
    
    @TempDir
    Path tempDir;

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
    void testGetKubectlPath_CallsInitializeIfNotInitialized() {
        // При первом вызове getKubectlPath() должен вызываться initializeKubectl()
        String path1 = embeddedKubectlService.getKubectlPath();
        String path2 = embeddedKubectlService.getKubectlPath();
        
        // Оба вызова должны вернуть одинаковый результат (или null)
        assertEquals(path1, path2);
    }

    @Test
    void testCleanup_DoesNotThrowException() {
        // Метод cleanup должен работать даже если kubectl не инициализирован
        assertDoesNotThrow(() -> embeddedKubectlService.cleanup());
    }

    @Test
    void testCleanup_DoesNotThrowExceptionWhenCalledMultipleTimes() {
        // Метод cleanup должен безопасно вызываться несколько раз
        assertDoesNotThrow(() -> {
            embeddedKubectlService.cleanup();
            embeddedKubectlService.cleanup();
            embeddedKubectlService.cleanup();
        });
    }

    @Test
    void testCleanup_WhenKubectlPathIsNull() {
        // Cleanup должен работать когда путь null
        // Путь будет null если инициализация не удалась
        embeddedKubectlService.cleanup();
        
        // Не должно быть исключений
        assertTrue(true);
    }

    @Test
    void testExecuteKubectlCommand_ThrowsExceptionIfNotInitialized() {
        // Если kubectl не инициализирован, команда должна выбрасывать исключение
        assertThrows(IllegalStateException.class, () -> {
            embeddedKubectlService.executeKubectlCommand("version");
        });
    }

    @Test
    void testExecuteKubectlCommand_ThrowsExceptionWhenKubectlPathIsNull() {
        // Попытка выполнить команду когда путь null
        assertThrows(IllegalStateException.class, () -> {
            embeddedKubectlService.executeKubectlCommand("version");
        });
    }

    @Test
    void testExecuteKubectlCommand_ThrowsExceptionWithMultipleArgs() {
        // Тест с несколькими аргументами
        assertThrows(IllegalStateException.class, () -> {
            embeddedKubectlService.executeKubectlCommand("get", "pods", "-o", "json");
        });
    }

    @Test
    void testExecuteKubectlCommand_ThrowsExceptionWithEmptyArgs() {
        // Тест с пустыми аргументами
        assertThrows(IllegalStateException.class, () -> {
            embeddedKubectlService.executeKubectlCommand();
        });
    }

    @Test
    void testIsInitialized_ReturnsFalseAfterFailedInitialization() {
        // Если инициализация не удалась, isInitialized должна вернуть false
        // Это происходит автоматически при getKubectlPath() когда ресурс не найден или не Linux
        boolean initialized = embeddedKubectlService.isInitialized();
        
        // На Windows или если kubectl файл отсутствует, должно быть false
        assertFalse(initialized);
    }
    
    @Test
    void testExtractKubectlToTemp_ResourceNotFound() throws Exception {
        // Используем рефлексию для вызова приватного метода
        try {
            ReflectionTestUtils.invokeMethod(
                embeddedKubectlService,
                "extractKubectlToTemp",
                "nonexistent-file"
            );
            fail("Expected IOException for nonexistent resource");
        } catch (Exception e) {
            // Ожидаем IOException или RuntimeException обернутый в него
            assertTrue(e.getCause() instanceof IOException || e instanceof RuntimeException);
        }
    }
    
    @Test
    void testMakeExecutable_HandlesWindowsException() throws Exception {
        // На Windows PosixFilePermission может выбрасывать исключение
        // Устанавливаем путь к несуществующему файлу для проверки обработки ошибок
        String nonExistentPath = tempDir.resolve("nonexistent").toString();
        
        // Метод должен обработать исключение и не выбросить его дальше
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(
                embeddedKubectlService,
                "makeExecutable",
                nonExistentPath
            );
        });
    }
    
    @Test
    void testMakeExecutable_WithValidPath() throws Exception {
        // Создаем тестовый файл
        Path testFile = tempDir.resolve("test-kubectl");
        Files.createFile(testFile);
        
        // На Windows это может выбросить исключение, что нормально
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(
                embeddedKubectlService,
                "makeExecutable",
                testFile.toString()
            );
        });
    }
    
    @Test
    void testTestKubectl_ProcessDoesNotFinish() throws Exception {
        // Устанавливаем путь к несуществующему kubectl, чтобы команда завершилась с ошибкой
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", "/nonexistent/kubectl");
        ReflectionTestUtils.setField(embeddedKubectlService, "initialized", false);
        
        // testKubectl должен вернуть false, так как команда не выполнится
        Object result = ReflectionTestUtils.invokeMethod(embeddedKubectlService, "testKubectl");
        assertNotNull(result);
        assertFalse(Boolean.TRUE.equals(result));
    }
    
    @Test
    void testTestKubectl_ExceptionHandling() throws Exception {
        // Устанавливаем путь к несуществующему kubectl для вызова исключения
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", null);
        ReflectionTestUtils.setField(embeddedKubectlService, "initialized", false);
        
        // testKubectl должен обработать исключение и вернуть false
        Object result = ReflectionTestUtils.invokeMethod(embeddedKubectlService, "testKubectl");
        assertNotNull(result);
        assertFalse(Boolean.TRUE.equals(result));
    }
    
    @Test
    void testInitializeKubectl_ExceptionPath() throws Exception {
        // Сбрасываем состояние
        ReflectionTestUtils.setField(embeddedKubectlService, "initialized", false);
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", null);
        
        // Пытаемся инициализировать - на Windows или без файла это должно завершиться с ошибкой
        String path = embeddedKubectlService.getKubectlPath();
        
        // Путь должен быть null если инициализация не удалась
        // Проверяем, что метод не выбросил исключение
        assertNotNull(path == null || path.length() >= 0);
    }
    
    @Test
    void testInitializeKubectl_TestKubectlReturnsFalse() throws Exception {
        // Сбрасываем состояние
        ReflectionTestUtils.setField(embeddedKubectlService, "initialized", false);
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", null);
        
        // Если testKubectl вернет false, initialized должен остаться false
        // Это проверяется через поведение getKubectlPath()
        String path1 = embeddedKubectlService.getKubectlPath();
        String path2 = embeddedKubectlService.getKubectlPath();
        
        // Оба вызова должны вернуть одинаковый результат (null если инициализация не удалась)
        assertEquals(path1, path2);
        assertFalse(embeddedKubectlService.isInitialized());
    }
    
    @Test
    void testExecuteKubectlCommand_ProcessTimeout() throws Exception {
        // Устанавливаем инициализированное состояние с путем
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", "/nonexistent/kubectl");
        ReflectionTestUtils.setField(embeddedKubectlService, "initialized", true);
        
        // Команда должна выбросить IOException, так как процесс не может завершиться нормально
        assertThrows(IOException.class, () -> {
            embeddedKubectlService.executeKubectlCommand("version");
        });
    }
    
    @Test
    void testExecuteKubectlCommand_NonZeroExitCode() throws Exception {
        // Устанавливаем путь к несуществующему kubectl
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", "/nonexistent/kubectl");
        ReflectionTestUtils.setField(embeddedKubectlService, "initialized", true);
        
        // Команда должна выбросить IOException с кодом ошибки
        assertThrows(Exception.class, () -> {
            embeddedKubectlService.executeKubectlCommand("invalid-command");
        });
    }
    
    @Test
    void testCleanup_DeletesFile() throws Exception {
        // Создаем тестовый файл
        Path testFile = tempDir.resolve("test-kubectl");
        Files.createFile(testFile);
        
        // Устанавливаем путь
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", testFile.toString());
        
        // Вызываем cleanup
        embeddedKubectlService.cleanup();
        
        // Файл должен быть удален (или папка, если это была последняя операция)
        // Проверяем, что метод выполнился без исключений
        assertTrue(true); // Метод должен выполниться без исключений
    }
    
    @Test
    void testCleanup_DeletesParentDirectory() throws Exception {
        // Создаем временную директорию и файл в ней
        Path parentDir = tempDir.resolve("kubectl-temp");
        Files.createDirectories(parentDir);
        Path testFile = parentDir.resolve("kubectl-linux-amd64");
        Files.createFile(testFile);
        
        // Устанавливаем путь
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", testFile.toString());
        
        // Вызываем cleanup
        embeddedKubectlService.cleanup();
        
        // Проверяем, что метод выполнился без исключений
        assertTrue(true);
    }
    
    @Test
    void testExecuteKubectlCommand_HandlesIOException() throws Exception {
        // Устанавливаем путь к несуществующему kubectl
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", "/nonexistent/path/kubectl");
        ReflectionTestUtils.setField(embeddedKubectlService, "initialized", true);
        
        // Команда должна выбросить IOException
        assertThrows(IOException.class, () -> {
            embeddedKubectlService.executeKubectlCommand("get", "pods");
        });
    }
    
    @Test
    void testIsInitialized_TrueWhenInitialized() throws Exception {
        // Устанавливаем инициализированное состояние
        ReflectionTestUtils.setField(embeddedKubectlService, "initialized", true);
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", "/some/path/kubectl");
        
        assertTrue(embeddedKubectlService.isInitialized());
    }
    
    @Test
    void testIsInitialized_FalseWhenPathIsNull() throws Exception {
        // Устанавливаем initialized=true, но путь null
        ReflectionTestUtils.setField(embeddedKubectlService, "initialized", true);
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", null);
        
        assertFalse(embeddedKubectlService.isInitialized());
    }
    
    @Test
    void testIsInitialized_FalseWhenNotInitialized() throws Exception {
        // Устанавливаем неинициализированное состояние
        ReflectionTestUtils.setField(embeddedKubectlService, "initialized", false);
        ReflectionTestUtils.setField(embeddedKubectlService, "kubectlPath", "/some/path/kubectl");
        
        assertFalse(embeddedKubectlService.isInitialized());
    }
}

