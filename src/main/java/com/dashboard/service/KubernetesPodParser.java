package com.dashboard.service;

import com.dashboard.model.PodInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Парсер для извлечения информации о подах из JSON ответов kubectl
 */
@Component
public class KubernetesPodParser {
    
    private static final Logger logger = LoggerFactory.getLogger(KubernetesPodParser.class);
    
    private final ObjectMapper objectMapper;
    
    public KubernetesPodParser() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Парсит JSON вывод kubectl и извлекает информацию о подах
     * 
     * @param jsonOutput - JSON строка от kubectl команды
     * @return список объектов PodInfo с извлеченной информацией
     */
    public java.util.List<PodInfo> parseKubectlOutput(String jsonOutput) {
        java.util.List<PodInfo> pods = new java.util.ArrayList<>();
        
        try {
            logger.debug("Парсинг JSON kubectl output, длина: {}", jsonOutput.length());
            
            // Парсим JSON с помощью Jackson
            JsonNode rootNode = objectMapper.readTree(jsonOutput);
            // Извлекаем массив items, который содержит информацию о подах
            JsonNode itemsNode = rootNode.get("items");
            
            if (itemsNode != null && itemsNode.isArray()) {
                logger.debug("Найден массив items с {} элементами", itemsNode.size());
                
                // Проходим по каждому элементу массива
                for (JsonNode itemNode : itemsNode) {
                    PodInfo podInfo = parsePodJson(itemNode);
                    if (podInfo != null) {
                        // Извлекаем дополнительную информацию: POD_NAME, рестарты, время до Ready
                        extractAdditionalPodInfo(itemNode, podInfo);
                        
                        pods.add(podInfo);
                        logger.debug("Добавлен под: {} (POD_NAME: {})", podInfo.getName(), podInfo.getPodName());
                    }
                }
                
                logger.info("Успешно распарсено {} подов", pods.size());
                
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
    public PodInfo parsePodJson(JsonNode podNode) {
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
                    LocalDateTime creationDate = KubernetesUtils.parseKubernetesTimestamp(creationTimestamp);
                    podInfo.setCreationDate(creationDate);
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
                    // Очищаем имя образа от registry префиксов
                    image = KubernetesUtils.cleanImageName(image);
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
                                        // Извлекаем только GC-связанные опции
                                        String gcOptions = KubernetesUtils.extractGcOptions(javaToolOptions);
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
            
            // === ИЗВЛЕЧЕНИЕ СТАТУСА (рестарты) ===
            JsonNode statusNode = podNode.get("status");
            if (statusNode != null) {
                // Суммарное количество рестартов по всем контейнерам
                if (statusNode.has("containerStatuses") && statusNode.get("containerStatuses").isArray()) {
                    int restartSum = 0;
                    for (JsonNode cs : statusNode.get("containerStatuses")) {
                        if (cs.has("restartCount")) {
                            restartSum += cs.get("restartCount").asInt(0);
                        }
                    }
                    podInfo.setRestarts(restartSum);
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
     * Извлекает дополнительную информацию о поде и устанавливает её напрямую в PodInfo
     * 
     * Извлекает:
     * - Полное имя пода из metadata.name
     * - Количество рестартов из status.containerStatuses[].restartCount
     * - Время от создания до статуса Ready (разница между creationTimestamp и Ready condition lastTransitionTime)
     * 
     * @param podNode - JSON узел с информацией о поде
     * @param podInfo - объект PodInfo для заполнения данными
     */
    public void extractAdditionalPodInfo(JsonNode podNode, PodInfo podInfo) {
        try {
            JsonNode metadataNode = podNode.get("metadata");
            
            // Извлекаем полное имя пода из metadata.name
            if (metadataNode != null && metadataNode.has("name")) {
                podInfo.setPodName(metadataNode.get("name").asText());
            }
            
            // Извлекаем время создания пода
            LocalDateTime creationTime = null;
            if (metadataNode != null && metadataNode.has("creationTimestamp")) {
                String creationTimestamp = metadataNode.get("creationTimestamp").asText();
                creationTime = KubernetesUtils.parseKubernetesTimestamp(creationTimestamp);
            }
            
            // Извлекаем время когда под стал Ready (из conditions)
            JsonNode statusNode = podNode.get("status");
            LocalDateTime readyTime = null;
            if (statusNode != null && statusNode.has("conditions") && 
                statusNode.get("conditions").isArray()) {
                
                for (JsonNode condition : statusNode.get("conditions")) {
                    if (condition.has("type") && "Ready".equals(condition.get("type").asText()) &&
                        condition.has("status") && "True".equals(condition.get("status").asText()) &&
                        condition.has("lastTransitionTime")) {
                        
                        String lastTransitionTimeStr = condition.get("lastTransitionTime").asText();
                        readyTime = KubernetesUtils.parseKubernetesTimestamp(lastTransitionTimeStr);
                        break; // Нашли условие Ready
                    }
                }
            }
            
            // Вычисляем разницу между созданием и статусом Ready
            if (creationTime != null && readyTime != null) {
                long seconds = Duration.between(creationTime, readyTime).getSeconds();
                String readyTimeStr = KubernetesUtils.formatDuration(seconds);
                podInfo.setReadyTime(readyTimeStr);
            } else {
                podInfo.setReadyTime("-");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при извлечении дополнительной информации о поде: {}", e.getMessage(), e);
        }
    }
}

