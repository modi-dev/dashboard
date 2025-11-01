package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с Kubernetes API (оркестрационный слой)
 * 
 * Делегирует выполнение команд kubectl в KubectlCommandExecutor,
 * парсинг JSON в KubernetesPodParser и утилитные операции в KubernetesUtils.
 * 
 * Основные функции:
 * - Получение информации о запущенных подах в Kubernetes кластере
 * - Генерация HTML страниц с информацией о подах
 * - Работа с namespace и конфигурацией кластера
 * - Получение версии Kubernetes
 */
@Service
public class KubernetesService {
    
    private static final Logger logger = LoggerFactory.getLogger(KubernetesService.class);
    
    @Autowired
    private KubernetesConfig kubernetesConfig;
    
    @Autowired
    private KubectlCommandExecutor kubectlExecutor;
    
    @Autowired
    private KubernetesPodParser podParser;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Получает конфигурацию Kubernetes
     */
    public KubernetesConfig getKubernetesConfig() {
        return kubernetesConfig;
    }
    
    /**
     * Получает информацию о всех запущенных подах в namespace
     * 
     * Алгоритм работы:
     * 1. Проверяет, включена ли интеграция с Kubernetes
     * 2. Выполняет команду kubectl через KubectlCommandExecutor
     * 3. Парсит JSON ответ через KubernetesPodParser
     * 
     * @return список информации о подах
     */
    public List<PodInfo> getRunningPods() {
        List<PodInfo> pods = new ArrayList<>();
        
        try {
            // Проверяем, включена ли интеграция с Kubernetes
            if (!kubernetesConfig.isEnabled()) {
                logger.warn("Kubernetes интеграция отключена. Установите kubernetes.enabled=true для активации");
                return pods;
            }
            
            logger.info("Получение информации о подах в namespace: {}", kubernetesConfig.getNamespace());
            
            // Выполняем команду kubectl для получения только запущенных подов в JSON формате
            // --field-selector=status.phase==Running - фильтрует только запущенные поды
            // -o json - выводит результат в JSON формате для удобного парсинга
            String jsonOutput = kubectlExecutor.executeCommand(
                "get", "pods",
                "--field-selector=status.phase==Running",
                "-n", kubernetesConfig.getNamespace(),
                "-o", "json"
            );
            
            // Парсим JSON и извлекаем информацию о подах
            pods = podParser.parseKubectlOutput(jsonOutput);
            logger.info("Успешно получена информация о {} подах", pods.size());
            
        } catch (KubectlException e) {
            logger.error("Ошибка выполнения kubectl команды: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Ошибка при получении информации о подах: {}", e.getMessage(), e);
        }
        
        return pods;
    }
    
    /**
     * Парсит JSON вывод kubectl и извлекает информацию о подах
     * 
     * Делегирует парсинг в KubernetesPodParser для обратной совместимости
     * 
     * @param jsonOutput - JSON строка от kubectl команды
     * @return список объектов PodInfo с извлеченной информацией
     */
    public List<PodInfo> parseKubectlOutput(String jsonOutput) {
        return podParser.parseKubectlOutput(jsonOutput);
    }
    
    /**
     * Получает текущий активный namespace из конфигурации kubectl
     * 
     * Использует команду: kubectl config view --minify -o jsonpath='{.contexts[0].context.namespace}'
     * 
     * @return имя текущего namespace или "default" если не удалось определить
     */
    public String getCurrentNamespace() {
        try {
            String namespace = kubectlExecutor.executeCommand(
                "config", "view",
                "--minify",
                "-o", "jsonpath={.contexts[0].context.namespace}"
            );
            
            if (namespace == null || namespace.trim().isEmpty()) {
                return "default";
            }
            
            return namespace.trim();
            
        } catch (KubectlException e) {
            logger.warn("Не удалось получить текущий namespace: {}", e.getMessage());
            // В случае ошибки возвращаем namespace из конфигурации
            return kubernetesConfig.getNamespace();
        } catch (Exception e) {
            logger.warn("Не удалось получить текущий namespace: {}", e.getMessage());
            return kubernetesConfig.getNamespace();
        }
    }
    
    /**
     * Подсчитывает количество уникальных сервисов из списка подов
     * (уникальные значения поля name из подов)
     * 
     * @param pods список подов для анализа
     * @return количество уникальных сервисов
     */
    public long countUniqueServices(List<PodInfo> pods) {
        return pods.stream()
                .map(PodInfo::getName)
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .count();
    }
    
    /**
     * Получает версию Kubernetes кластера
     * 
     * Использует команду: kubectl version -o json
     * 
     * @return версия Kubernetes или "Неизвестно" если не удалось определить
     */
    public String getKubernetesVersion() {
        try {
            String json = kubectlExecutor.executeCommand("version", "-o", "json");
            
            if (json == null || json.trim().isEmpty()) {
                return "Неизвестно";
            }
            
            JsonNode root = objectMapper.readTree(json);
            // Пытаемся взять версию сервера, иначе клиента
            JsonNode server = root.get("serverVersion");
            if (server != null) {
                JsonNode gitVersion = server.get("gitVersion");
                if (gitVersion != null && !gitVersion.asText().isBlank()) {
                    return gitVersion.asText();
                }
            }
            JsonNode client = root.get("clientVersion");
            if (client != null) {
                JsonNode gitVersion = client.get("gitVersion");
                if (gitVersion != null && !gitVersion.asText().isBlank()) {
                    return gitVersion.asText();
                }
            }
            return "Неизвестно";
        } catch (KubectlException e) {
            logger.warn("Не удалось получить версию Kubernetes: {}", e.getMessage());
            return "Неизвестно";
        } catch (Exception e) {
            logger.warn("Не удалось получить версию Kubernetes: {}", e.getMessage());
            return "Неизвестно";
        }
    }
    
    /**
     * Генерирует HTML страницу с информацией о подах (аналог оригинального version.sh скрипта)
     * 
     * Создает полноценную HTML страницу с:
     * - CSS стилями для таблицы (стиль IKSWEB)
     * - Информацией о времени обновления и namespace
     * - Таблицей с данными всех запущенных подов
     * 
     * Структура таблицы:
     * - NAME: имя приложения
     * - VERSION: версия Docker образа
     * - MsBranch: ветка микросервиса
     * - ConfigBranch: ветка конфигурации
     * - GC: настройки сборщика мусора (извлеченные из JAVA_TOOL_OPTIONS)
     * - CREATION DATE: дата создания пода
     * - PORT: порты контейнера (все порты через запятую)
     * - REPLICAS: количество реплик (одинаковых подов)
     * - REQUEST: запрошенные ресурсы (CPU/RAM)
     * 
     * @return HTML строка с полной страницей
     */
    public String generateHtmlPage() {
        // Получаем актуальную информацию о подах
        List<PodInfo> pods = getRunningPods();
        String currentNamespace = getCurrentNamespace();
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " UTC";
        
        StringBuilder html = new StringBuilder();
        
        // === CSS СТИЛИ (из оригинального скрипта) ===
        html.append("<style>");
        html.append("/* Стили таблицы (IKSWEB) */");
        html.append("table.iksweb{text-decoration: none;border-collapse:collapse;width:100%;text-align:center;}");
        html.append("table.iksweb th{font-weight:normal;font-size:14px; color:#ffffff;background-color:#354251;}");
        html.append("table.iksweb td{font-size:14px;color:#354251;}");
        html.append("table.iksweb td,table.iksweb th{white-space:pre-wrap;padding:10px 5px;line-height:13px;vertical-align: middle;border: 1px solid #354251;}");
        html.append("table.iksweb tr:hover{background-color:#f9fafb}");
        html.append("table.iksweb tr:hover td{color:#354251;cursor:default;}");
        html.append("</style>");
        
        // === HTML СТРУКТУРА ===
        html.append("<html><body>");
        // Заголовок с временем обновления и namespace
        html.append("<p style=\"font-size:12px\">update time: ").append(currentDate).append("<br>namespace: ").append(currentNamespace).append("</p><br>");
        
        // Создаем таблицу с заголовками
        html.append("<table class='iksweb'>");
        html.append("<thead><tr>");
        html.append("<th>NAME</th>");
        html.append("<th>VERSION</th>");
        html.append("<th>MsBranch</th>");
        html.append("<th>ConfigBranch</th>");
        html.append("<th>GC</th>");
        html.append("<th>CREATION DATE</th>");
        html.append("<th>PORT</th>");
        html.append("<th>REQUEST</th>");
        html.append("</tr></thead>");
        
        // === ДОБАВЛЯЕМ СТРОКИ С ДАННЫМИ ПОДОВ ===
        for (PodInfo pod : pods) {
            html.append("<tr>");
            html.append("<td align=\"left\">").append(pod.getName() != null ? pod.getName() : "").append("</td>");
            html.append("<td align=\"left\">").append(pod.getVersion() != null ? pod.getVersion() : "").append("</td>");
            html.append("<td>").append(pod.getMsBranch() != null ? pod.getMsBranch() : "").append("</td>");
            html.append("<td>").append(pod.getConfigBranch() != null ? pod.getConfigBranch() : "").append("</td>");
            html.append("<td>").append(pod.getGcOptions() != null ? pod.getGcOptions() : "").append("</td>");
            html.append("<td>").append(pod.getCreationDate() != null ? pod.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "").append("</td>");
            html.append("<td align=\"left\">").append(pod.getPort() != null ? pod.getPort() : "").append("</td>");
            html.append("<td align=\"left\">");
            html.append("CPU: ").append(pod.getCpuRequest() != null ? pod.getCpuRequest() : "").append("<br><br>");
            html.append("RAM: ").append(pod.getMemoryRequest() != null ? pod.getMemoryRequest() : "");
            html.append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("</html></body>");
        
        return html.toString();
    }
}
