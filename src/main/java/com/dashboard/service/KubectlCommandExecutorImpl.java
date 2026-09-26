package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Реализация KubectlCommandExecutor.
 * Выполняет команды kubectl через ProcessBuilder с таймаутом и параллельным чтением stdout/stderr.
 */
@Component
public class KubectlCommandExecutorImpl implements KubectlCommandExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(KubectlCommandExecutorImpl.class);
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    private static final long STREAM_DRAIN_TIMEOUT_SECONDS = 5;
    
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
        String embeddedPath = embeddedKubectlService.getKubectlPath();
        if (embeddedPath != null && embeddedKubectlService.isInitialized()) {
            logger.debug("Используем встроенный kubectl: {}", embeddedPath);
            return embeddedPath;
        }
        
        String configPath = kubernetesConfig.getKubectlPath();
        logger.debug("Используем kubectl из конфигурации: {}", configPath);
        return configPath;
    }
    
    @Override
    public String executeCommand(String... args) throws KubectlException {
        Process process = null;
        try {
            String kubectlPath = getKubectlPath();
            
            String[] command = new String[args.length + 1];
            command[0] = kubectlPath;
            System.arraycopy(args, 0, command, 1, args.length);
            
            logger.debug("Выполняем команду kubectl: {}", String.join(" ", command));
            
            process = new ProcessBuilder(command).start();
            
            // Читаем stdout и stderr параллельно, чтобы pipe не блокировал процесс
            Process running = process;
            CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(
                () -> readStream(running.getInputStream()));
            CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(
                () -> readStream(running.getErrorStream()));
            
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                destroyProcess(process);
                stdoutFuture.cancel(true);
                stderrFuture.cancel(true);
                throw new KubectlException(
                    String.format("Команда kubectl не завершилась в течение %d секунд", timeoutSeconds)
                );
            }
            
            String output = awaitStream(stdoutFuture, "stdout");
            String error = awaitStream(stderrFuture, "stderr");
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                logger.error("kubectl завершился с кодом {}: {}", exitCode, error);
                throw new KubectlException(
                    String.format("Команда kubectl завершилась с ошибкой (код: %d)", exitCode),
                    exitCode,
                    error
                );
            }
            
            return output;
            
        } catch (KubectlException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Ошибка при выполнении команды kubectl: {}", e.getMessage(), e);
            throw new KubectlException("Ошибка при выполнении команды kubectl: " + e.getMessage(), e);
        } finally {
            if (process != null && process.isAlive()) {
                destroyProcess(process);
            }
        }
    }
    
    private static String readStream(InputStream inputStream) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(line);
            }
        } catch (Exception e) {
            // Поток мог быть закрыт при destroyForcibly / cancel — это ожидаемо
            logger.debug("Чтение потока kubectl прервано: {}", e.getMessage());
        }
        return builder.toString();
    }
    
    private static String awaitStream(CompletableFuture<String> future, String streamName) throws Exception {
        try {
            return future.get(STREAM_DRAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new KubectlException("Таймаут чтения " + streamName + " команды kubectl");
        }
    }
    
    private static void destroyProcess(Process process) {
        process.destroyForcibly();
        try {
            process.waitFor(STREAM_DRAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
