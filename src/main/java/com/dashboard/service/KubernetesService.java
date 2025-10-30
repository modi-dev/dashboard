package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для работы с Kubernetes API
 * Интегрирует функциональность из version.sh скрипта
 * 
 * Основные функции:
 * - Получение информации о запущенных подах в Kubernetes кластере
 * - Парсинг JSON ответов от kubectl команд
 * - Генерация HTML страниц с информацией о подах
 * - Работа с namespace и конфигурацией кластера
 */
@Service
public class KubernetesService {
    
    // Логгер для записи отладочной информации и ошибок
    private static final Logger logger = LoggerFactory.getLogger(KubernetesService.class);
    
    // Конфигурация Kubernetes (путь к kubectl, namespace, включение/отключение)
    @Autowired
    private KubernetesConfig kubernetesConfig;
    
    // Встроенный kubectl сервис
    @Autowired
    private EmbeddedKubectlService embeddedKubectlService;
    
    // Jackson ObjectMapper для парсинга JSON ответов от kubectl
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Получает конфигурацию Kubernetes
     */
    public KubernetesConfig getKubernetesConfig() {
        return kubernetesConfig;
    }
    
    /**
     * Получает путь к kubectl с приоритетом встроенного
     * 
     * @return путь к kubectl (встроенный или из конфигурации)
     */
    private String getKubectlPath() {
        // Сначала пытаемся использовать встроенный kubectl (инициализация по требованию)
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
    
    /**
     * Получает информацию о всех запущенных подах в namespace
     * 
     * Алгоритм работы:
     * 1. Проверяет, включена ли интеграция с Kubernetes
     * 2. Формирует команду kubectl для получения запущенных подов
     * 3. Выполняет команду через ProcessBuilder (современная замена Runtime.exec)
     * 4. Читает JSON ответ и ошибки из потоков процесса
     * 5. Парсит JSON и извлекает информацию о подах
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
            logger.info("Используется kubectl: {}", getKubectlPath());
            
            // Формируем команду kubectl для получения только запущенных подов в JSON формате
            // --field-selector=status.phase==Running - фильтрует только запущенные поды
            // -o json - выводит результат в JSON формате для удобного парсинга
            String command = String.format("%s get pods --field-selector=status.phase==Running -n %s -o json", 
                                         getKubectlPath(), kubernetesConfig.getNamespace());
            
            logger.debug("Выполняем команду: {}", command);
            
            // Используем ProcessBuilder вместо устаревшего Runtime.exec для выполнения команды
            Process process = new ProcessBuilder(command.split("\\s+")).start();
            // Создаем читателей для стандартного вывода и потока ошибок
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            // Буферы для накопления вывода команды
            StringBuilder jsonOutput = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            
            // Читаем стандартный вывод команды (JSON ответ)
            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }
            
            // Читаем поток ошибок команды
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            
            // Ждем завершения процесса и получаем код выхода
            int exitCode = process.waitFor();
            logger.debug("kubectl exit code: {}", exitCode);
            logger.debug("kubectl output: {}", jsonOutput.toString());
            
            // Логируем ошибки, если они есть
            if (errorOutput.length() > 0) {
                logger.warn("kubectl stderr: {}", errorOutput.toString());
            }
            
            // Обрабатываем результат выполнения команды
            if (exitCode == 0) {
                // Команда выполнена успешно - парсим JSON и извлекаем информацию о подах
                pods = parseKubectlOutput(jsonOutput.toString());
                logger.info("Успешно получена информация о {} подах", pods.size());
            } else {
                // Команда завершилась с ошибкой - логируем детали
                logger.error("Ошибка выполнения kubectl команды. Код выхода: {}. Ошибка: {}", 
                           exitCode, errorOutput.toString());
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при получении информации о подах: {}", e.getMessage(), e);
        }
        
        return pods;
    }
    
    /**
     * Парсит JSON вывод kubectl и извлекает информацию о подах
     * Группирует одинаковые поды и подсчитывает количество реплик
     * 
     * Структура JSON ответа kubectl:
     * {
     *   "items": [
     *     {
     *       "kind": "Pod",
     *       "metadata": { ... },
     *       "spec": { ... }
     *     }
     *   ]
     * }
     * 
     * @param jsonOutput - JSON строка от kubectl команды
     * @return список объектов PodInfo с извлеченной информацией (сгруппированные по уникальности)
     */
    public List<PodInfo> parseKubectlOutput(String jsonOutput) {
        List<PodInfo> pods = new ArrayList<>();
        
        try {
            logger.debug("Парсинг JSON kubectl output, длина: {}", jsonOutput.length());
            
            // Парсим JSON с помощью Jackson
            JsonNode rootNode = objectMapper.readTree(jsonOutput);
            // Извлекаем массив items, который содержит информацию о подах
            JsonNode itemsNode = rootNode.get("items");
            
            if (itemsNode != null && itemsNode.isArray()) {
                logger.debug("Найден массив items с {} элементами", itemsNode.size());
                
                // Карта для группировки подов по уникальному ключу
                Map<String, PodInfo> podMap = new HashMap<>();
                
                // Проходим по каждому элементу массива
                for (JsonNode itemNode : itemsNode) {
                    PodInfo podInfo = parsePodJson(itemNode);
                    if (podInfo != null) {
                        // Создаем уникальный ключ для группировки (имя + версия + ветки)
                        String uniqueKey = createUniqueKey(podInfo);
                        logger.info("Ключ группировки для пода {}: '{}'", podInfo.getName(), uniqueKey);
                        
                        if (podMap.containsKey(uniqueKey)) {
                            // Увеличиваем счетчик реплик для существующего пода
                            PodInfo existingPod = podMap.get(uniqueKey);
                            existingPod.setReplicas(existingPod.getReplicas() + 1);
                            logger.info("Увеличено количество реплик для пода: {} (теперь: {})", 
                                       existingPod.getName(), existingPod.getReplicas());
                        } else {
                            // Добавляем новый под с количеством реплик = 1
                            podInfo.setReplicas(1);
                            podMap.put(uniqueKey, podInfo);
                            logger.info("Добавлен новый под: {} (реплик: 1)", podInfo.getName());
                        }
                    }
                }
                
                // Преобразуем карту в список и сортируем по количеству реплик (по убыванию)
                pods.addAll(podMap.values());
                pods.sort((p1, p2) -> {
                    int replicas1 = p1.getReplicas() != null ? p1.getReplicas() : 1;
                    int replicas2 = p2.getReplicas() != null ? p2.getReplicas() : 1;
                    return Integer.compare(replicas2, replicas1); // Сортировка по убыванию
                });
                logger.info("Найдено {} уникальных подов из {} общих, успешно распарсено: {}", 
                           podMap.size(), itemsNode.size(), pods.size());
                
            } else {
                logger.warn("Не найден массив items в JSON");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при парсинге JSON kubectl: {}", e.getMessage(), e);
        }
        
        return pods;
    }
    
    /**
     * Парсит JSON отдельного пода и извлекает всю необходимую информацию
     * 
     * Извлекаемые данные:
     * - Имя приложения из labels.app
     * - Дата создания из creationTimestamp
     * - Ветки из аннотаций (ms-branch, config-branch)
     * - Версия образа из spec.containers[0].image
     * - Порты из spec.containers[0].ports[].containerPort (все порты через запятую)
     * - GC опции из переменных окружения JAVA_TOOL_OPTIONS
     * - Ресурсы (CPU, память) из spec.containers[0].resources.requests
     * 
     * @param podNode - JSON узел с информацией о поде
     * @return объект PodInfo с извлеченными данными или null при ошибке
     */
    private PodInfo parsePodJson(JsonNode podNode) {
        try {
            PodInfo podInfo = new PodInfo();
            
            // === ИЗВЛЕЧЕНИЕ МЕТАДАННЫХ ===
            JsonNode metadataNode = podNode.get("metadata");
            if (metadataNode != null) {
                // Извлекаем имя приложения из labels.app
                JsonNode labelsNode = metadataNode.get("labels");
                if (labelsNode != null && labelsNode.has("app")) {
                    podInfo.setName(labelsNode.get("app").asText());
                }
                
                // Извлекаем дату создания пода
                if (metadataNode.has("creationTimestamp")) {
                    String creationTimestamp = metadataNode.get("creationTimestamp").asText();
                    try {
                        // Парсим ISO 8601 формат (убираем Z и парсим)
                        creationTimestamp = creationTimestamp.replace("Z", "");
                        LocalDateTime creationDate = LocalDateTime.parse(creationTimestamp, 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                        podInfo.setCreationDate(creationDate);
                    } catch (Exception e) {
                        logger.warn("Не удалось распарсить дату создания: {}", creationTimestamp);
                    }
                }
                
                // Извлекаем аннотации (ветки микросервиса и конфигурации)
                JsonNode annotationsNode = metadataNode.get("annotations");
                if (annotationsNode != null) {
                    if (annotationsNode.has("ms-branch")) {
                        podInfo.setMsBranch(annotationsNode.get("ms-branch").asText());
                    }
                    if (annotationsNode.has("config-branch")) {
                        podInfo.setConfigBranch(annotationsNode.get("config-branch").asText());
                    }
                }
            }
            
            // === ИЗВЛЕЧЕНИЕ ИНФОРМАЦИИ ИЗ КОНТЕЙНЕРОВ ===
            JsonNode specNode = podNode.get("spec");
            if (specNode != null) {
                JsonNode containersNode = specNode.get("containers");
                if (containersNode != null && containersNode.isArray() && containersNode.size() > 0) {
                    // Берем первый контейнер (обычно в поде один контейнер)
                    JsonNode mainContainer = null;
                    for (JsonNode container : containersNode) {
                        if ("main".equals(container.get("name").asText())) {
                            mainContainer = container;
                            break;
                        }
                    }
                    JsonNode infoContainer = (mainContainer != null) ? mainContainer : containersNode.get(0);
                    
                    // Извлекаем версию образа Docker
                    String image = infoContainer.get("image").asText();
                    // Очищаем имя образа от registry префиксов (как в оригинальном скрипте)
                    // Удаляем различные registry: pcss-prod, nexus, docker
                    image = image.replaceAll("pcss-prod[^:]*:", "")
                                .replaceAll("nexus[^:]*:", "")
                                .replaceAll("docker[^:]*:", "");
                    podInfo.setVersion(image);
                    
                    // Извлекаем все порты контейнера
                    if (infoContainer.has("ports")) {
                        JsonNode portsNode = infoContainer.get("ports");
                        if (portsNode.isArray() && portsNode.size() > 0) {
                            StringBuilder portsBuilder = new StringBuilder();
                            for (int i = 0; i < portsNode.size(); i++) {
                                JsonNode portNode = portsNode.get(i);
                                if (portNode.has("containerPort")) {
                                    if (i > 0) {
                                        portsBuilder.append(", ");
                                    }
                                    portsBuilder.append(portNode.get("containerPort").asText());
                                }
                            }
                            if (portsBuilder.length() > 0) {
                                podInfo.setPort(portsBuilder.toString());
                            }
                        }
                    }
                    
                    // Извлекаем переменные окружения (ищем JAVA_TOOL_OPTIONS для GC настроек)
                    if (infoContainer.has("env")) {
                        JsonNode envNode = infoContainer.get("env");
                        if (envNode.isArray()) {
                            for (JsonNode envVar : envNode) {
                                if (envVar.has("name") && "JAVA_TOOL_OPTIONS".equals(envVar.get("name").asText())) {
                                    if (envVar.has("value")) {
                                        String javaToolOptions = envVar.get("value").asText();
                                        // Извлекаем только GC-связанные опции с помощью регулярного выражения
                                        String gcOptions = extractGcOptions(javaToolOptions);
                                        podInfo.setGcOptions(gcOptions);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    
                    // Извлекаем ресурсы (CPU и память) из requests
                    if (infoContainer.has("resources")) {
                        JsonNode resourcesNode = infoContainer.get("resources");
                        if (resourcesNode.has("requests")) {
                            JsonNode requestsNode = resourcesNode.get("requests");
                            if (requestsNode.has("cpu")) {
                                podInfo.setCpuRequest(requestsNode.get("cpu").asText());
                            }
                            if (requestsNode.has("memory")) {
                                podInfo.setMemoryRequest(requestsNode.get("memory").asText());
                            }
                        }
                    }
                }
            }
            
            logger.debug("Распарсен под: name={}, version={}, port={}", 
                        podInfo.getName(), podInfo.getVersion(), podInfo.getPort());
            
            return podInfo;
            
        } catch (Exception e) {
            logger.error("Ошибка при парсинге пода: {}", e.getMessage(), e);
            return null;
        }
    }
    
    
    /**
     * Получает текущий активный namespace из конфигурации kubectl
     * 
     * Использует команду: kubectl config view --minify -o jsonpath='{.contexts[0].context.namespace}'
     * Эта команда извлекает namespace из текущего контекста kubectl
     * 
     * @return имя текущего namespace или "default" если не удалось определить
     */
    public String getCurrentNamespace() {
        try {
            // Команда для получения текущего namespace из kubectl конфигурации
            String command = String.format("%s config view --minify -o jsonpath='{.contexts[0].context.namespace}'", getKubectlPath());
            Process process = new ProcessBuilder(command.split("\\s+")).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            // Читаем результат команды
            String namespace = reader.readLine();
            if (namespace == null || namespace.trim().isEmpty()) {
                // Если namespace не определен, возвращаем "default"
                return "default";
            }
            
            return namespace.trim();
            
        } catch (Exception e) {
            logger.warn("Не удалось получить текущий namespace: {}", e.getMessage());
            // В случае ошибки возвращаем namespace из конфигурации
            return kubernetesConfig.getNamespace();
        }
    }
    
    /**
     * Получает версию Kubernetes кластера
     * 
     * Использует команду: kubectl version
     * Эта команда возвращает версию клиента kubectl и сервера
     * 
     * @return версия Kubernetes или "Неизвестно" если не удалось определить
     */
    public String getKubernetesVersion() {
        try {
            // Надежный способ: JSON-вывод, из которого достаем serverVersion.gitVersion
            String command = String.format("%s version -o json", getKubectlPath());
            Process process = new ProcessBuilder(command.split("\\s+")).start();

            BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            String line;
            while ((line = outReader.readLine()) != null) {
                output.append(line);
            }
            while ((line = errReader.readLine()) != null) {
                error.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.warn("kubectl version exited with code {}: {}", exitCode, error.toString());
                return "Неизвестно";
            }

            String json = output.toString();
            if (json.isEmpty()) {
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
        html.append("<th>REPLICAS</th>");
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
            html.append("<td align=\"center\">").append(pod.getReplicas() != null ? pod.getReplicas().toString() : "1").append("</td>");
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
    
    /**
     * Извлекает GC-связанные опции из строки JAVA_TOOL_OPTIONS
     * 
     * Регулярное выражение ищет опции, содержащие:
     * - GC в любом регистре (gc, GC, Gc)
     * - Опции начинающиеся с -XX: и содержащие GC
     * - Опции начинающиеся с -X и содержащие GC
     * - Опции начинающиеся с -XX:+Use и содержащие GC
     * - Опции начинающиеся с -XX:-Use и содержащие GC
     * 
     * @param javaToolOptions - строка с JAVA_TOOL_OPTIONS
     * @return строка с GC опциями, разделенными переносами строк
     */
    private String extractGcOptions(String javaToolOptions) {
        if (javaToolOptions == null || javaToolOptions.trim().isEmpty()) {
            return "";
        }
        
        // Регулярное выражение для поиска GC-связанных опций
        // Ищем опции, которые содержат "GC" в любом регистре или связаны с garbage collection
        Pattern gcPattern = Pattern.compile(
            "-XX:[+-]?[^\\s]*[Gg][Cc][^\\s]*|" +  // -XX: опции с GC
            "-X[^\\s]*[Gg][Cc][^\\s]*|" +        // -X опции с GC  
            "-XX:[+-]?Use[^\\s]*[Gg][Cc][^\\s]*|" + // -XX:+UseGC, -XX:-UseGC
            "-XX:[+-]?[Gg][Cc][^\\s]*|" +        // -XX:GC опции
            "-XX:[+-]?[^\\s]*[Gg][Cc]|" +        // опции заканчивающиеся на GC
            "-XX:[+-]?[^\\s]*[Gg]arbage[^\\s]*"  // garbage collection опции
        );
        
        Matcher matcher = gcPattern.matcher(javaToolOptions);
        StringBuilder gcOptions = new StringBuilder();
        
        while (matcher.find()) {
            String option = matcher.group().trim();
            if (!option.isEmpty()) {
                if (gcOptions.length() > 0) {
                    gcOptions.append("\n");
                }
                gcOptions.append(option);
            }
        }
        
        return gcOptions.toString();
    }
    
    /**
     * Создает уникальный ключ для группировки подов
     * 
     * Поды считаются одинаковыми, если у них совпадают:
     * - Имя приложения (name) - основная характеристика
     * - Версия образа (version) - основная характеристика
     * 
     * Остальные поля могут отличаться между репликами
     * 
     * @param podInfo - информация о поде
     * @return уникальный ключ для группировки
     */
    private String createUniqueKey(PodInfo podInfo) {
        StringBuilder keyBuilder = new StringBuilder();
        
        // Группируем только по основным характеристикам
        keyBuilder.append(podInfo.getName() != null ? podInfo.getName() : "").append("|");
        keyBuilder.append(podInfo.getVersion() != null ? podInfo.getVersion() : "");
        
        return keyBuilder.toString();
    }
}
