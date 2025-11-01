package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Реализация KubectlCommandExecutor
 * Выполняет команды kubectl через ProcessBuilder с таймаутами и обработкой ошибок
 */
@Component
public class KubectlCommandExecutorImpl implements KubectlCommandExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(KubectlCommandExecutorImpl.class);
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    
    private final KubernetesConfig kubernetesConfig;
    private final EmbeddedKubectlService embeddedKubectlService;
    private final long timeoutSeconds;
    
    @Autowired
    public KubectlCommandExecutorImpl(KubernetesConfig kubernetesConfig,
                                      EmbeddedKubectlService embeddedKubectlService,
                                      @Value("${kubernetes.command.timeout:30}") Long timeoutSeconds) {
        this.kubernetesConfig = kubernetesConfig;
        this.embeddedKubectlService = embeddedKubectlService;
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : DEFAULT_TIMEOUT_SECONDS;
    }
    
    /**
     * Получает путь к kubectl с приоритетом встроенного
     */
    private String getKubectlPath() {
        // Сначала пытаемся использовать встроенный kubectl
        String embeddedPath = embeddedKubectlService.getKubectlPath();
        if (embeddedPath != null && embeddedKubectlService.isInitialized()) {
            logger.debug("Используем встроенный kubectl: {}", embeddedPath);
            return embeddedPath;
        }
        
        // Если встроенный недоступен, используем путь из конфигурации
        String configPath = kubernetesConfig.getKubectlPath();
        logger.debug("Используем kubectl из конфигурации: {}", configPath);
        return configPath;
    }
    
    @Override
    public String executeCommand(String... args) throws KubectlException {
        try {
            String kubectlPath = getKubectlPath();
            
            // Формируем полную команду
            String[] command = new String[args.length + 1];
            command[0] = kubectlPath;
            System.arraycopy(args, 0, command, 1, args.length);
            
            logger.debug("Выполняем команду kubectl: {}", String.join(" ", command));
            
            // Запускаем процесс
            Process process = new ProcessBuilder(command).start();
            
            // Читаем stdout и stderr параллельно
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();
            
            try (BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                
                String line;
                // Читаем стандартный вывод
                while ((line = outReader.readLine()) != null) {
                    output.append(line);
                }
                
                // Читаем поток ошибок
                while ((line = errReader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }
            
            // Ждем завершения с таймаутом
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new KubectlException(
                    String.format("Команда kubectl не завершилась в течение %d секунд", timeoutSeconds)
                );
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String errorMsg = error.toString();
                logger.error("kubectl завершился с кодом {}: {}", exitCode, errorMsg);
                throw new KubectlException(
                    String.format("Команда kubectl завершилась с ошибкой (код: %d)", exitCode),
                    exitCode,
                    errorMsg
                );
            }
            
            return output.toString();
            
        } catch (KubectlException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Ошибка при выполнении команды kubectl: {}", e.getMessage(), e);
            throw new KubectlException("Ошибка при выполнении команды kubectl: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            executeCommand("version", "-o", "json");
            return true;
        } catch (KubectlException e) {
            logger.warn("kubectl недоступен: {}", e.getMessage());
            return false;
        }
    }
}

