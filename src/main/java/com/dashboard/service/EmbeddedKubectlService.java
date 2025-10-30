package com.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для работы с встроенным kubectl (Linux only)
 * 
 * Использует только Linux бинарник kubectl v1.34.1 из ресурсов JAR-файла.
 */
@Service
public class EmbeddedKubectlService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedKubectlService.class);
    
    private static final String KUBECTL_RESOURCE_PATH = "binaries/kubectl/";
    private static final String TEMP_DIR_PREFIX = "kubectl-";
    
    private String kubectlPath;
    private boolean initialized = false;

    /**
     * Получает путь к kubectl бинарнику
     * 
     * @return путь к kubectl или null если не удалось инициализировать
     */
    public String getKubectlPath() {
        if (!initialized) {
            initializeKubectl();
        }
        return kubectlPath;
    }

    /**
     * Инициализирует kubectl (Linux only)
     */
    private void initializeKubectl() {
        try {
            String kubectlFileName = "kubectl-linux-amd64";
            logger.info("Используем Linux kubectl v1.34.1: {}", kubectlFileName);
            
            // Извлекаем kubectl из classpath во временную папку
            kubectlPath = extractKubectlToTemp(kubectlFileName);
            
            // Делаем файл исполняемым
            makeExecutable(kubectlPath);
            
            // Проверяем, что kubectl работает
            if (testKubectl()) {
                initialized = true;
                logger.info("kubectl v1.34.1 (Linux) успешно инициализирован: {}", kubectlPath);
            } else {
                logger.error("kubectl не работает после инициализации");
                kubectlPath = null;
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при инициализации kubectl: {}", e.getMessage(), e);
            kubectlPath = null;
        }
    }


    /**
     * Извлекает kubectl из classpath во временную папку
     */
    private String extractKubectlToTemp(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(KUBECTL_RESOURCE_PATH + fileName);
        
        if (!resource.exists()) {
            throw new IOException("kubectl бинарник не найден: " + fileName);
        }
        
        // Создаем временную папку
        Path tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
        tempDir.toFile().deleteOnExit();
        
        // Извлекаем kubectl
        Path kubectlFile = tempDir.resolve(fileName);
        try (InputStream inputStream = resource.getInputStream();
             OutputStream outputStream = Files.newOutputStream(kubectlFile)) {
            
            inputStream.transferTo(outputStream);
        }
        
        logger.debug("kubectl извлечен во временную папку: {}", kubectlFile);
        return kubectlFile.toString();
    }

    /**
     * Делает файл исполняемым (для Unix-систем)
     */
    private void makeExecutable(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(path, permissions);
            logger.debug("Файл сделан исполняемым: {}", filePath);
        } catch (Exception e) {
            logger.warn("Не удалось сделать файл исполняемым: {}", e.getMessage());
        }
    }

    /**
     * Тестирует работу kubectl
     */
    private boolean testKubectl() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(kubectlPath, "version", "--client", "--short");
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // Ждем завершения процесса (максимум 10 секунд)
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                logger.error("kubectl не ответил в течение 10 секунд");
                return false;
            }
            
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                logger.debug("kubectl тест прошел успешно");
                return true;
            } else {
                logger.error("kubectl завершился с кодом: {}", exitCode);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при тестировании kubectl: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Выполняет команду kubectl
     * 
     * @param args аргументы команды
     * @return результат выполнения команды
     */
    public String executeKubectlCommand(String... args) throws IOException, InterruptedException {
        if (!initialized || kubectlPath == null) {
            throw new IllegalStateException("kubectl не инициализирован");
        }
        
        String[] command = new String[args.length + 1];
        command[0] = kubectlPath;
        System.arraycopy(args, 0, command, 1, args.length);
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // Читаем вывод
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // Ждем завершения процесса
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("kubectl не завершился в течение 30 секунд");
        }
        
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new IOException("kubectl завершился с кодом: " + exitCode + ", вывод: " + output.toString());
        }
        
        return output.toString();
    }

    /**
     * Проверяет, инициализирован ли kubectl
     */
    public boolean isInitialized() {
        return initialized && kubectlPath != null;
    }

    /**
     * Очищает временные файлы
     */
    public void cleanup() {
        if (kubectlPath != null) {
            try {
                Path path = Paths.get(kubectlPath);
                Files.deleteIfExists(path);
                // Удаляем родительскую папку если она пустая
                Path parentDir = path.getParent();
                if (parentDir != null && Files.exists(parentDir)) {
                    Files.deleteIfExists(parentDir);
                }
                logger.debug("Временные файлы kubectl очищены");
            } catch (Exception e) {
                logger.warn("Не удалось очистить временные файлы kubectl: {}", e.getMessage());
            }
        }
    }
}
