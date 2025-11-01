package com.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилитные методы для работы с Kubernetes данными
 */
public final class KubernetesUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(KubernetesUtils.class);
    
    private static final DateTimeFormatter KUBERNETES_TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    private static final Pattern GC_OPTIONS_PATTERN = Pattern.compile(
        "-XX:[+-]?[^\\s]*[Gg][Cc][^\\s]*|" +  // -XX: опции с GC
        "-X[^\\s]*[Gg][Cc][^\\s]*|" +        // -X опции с GC  
        "-XX:[+-]?Use[^\\s]*[Gg][Cc][^\\s]*|" + // -XX:+UseGC, -XX:-UseGC
        "-XX:[+-]?[Gg][Cc][^\\s]*|" +        // -XX:GC опции
        "-XX:[+-]?[^\\s]*[Gg][Cc]|" +        // опции заканчивающиеся на GC
        "-XX:[+-]?[^\\s]*[Gg]arbage[^\\s]*"  // garbage collection опции
    );
    
    private KubernetesUtils() {
        // Утилитный класс - запрещаем создание экземпляров
    }
    
    /**
     * Парсит timestamp в формате Kubernetes (ISO 8601)
     * 
     * @param timestamp строка в формате "yyyy-MM-dd'T'HH:mm:ssZ" или "yyyy-MM-dd'T'HH:mm:ss"
     * @return LocalDateTime или null если не удалось распарсить
     */
    public static LocalDateTime parseKubernetesTimestamp(String timestamp) {
        if (timestamp == null || timestamp.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Убираем Z в конце если есть
            String cleanedTimestamp = timestamp.replace("Z", "");
            return LocalDateTime.parse(cleanedTimestamp, KUBERNETES_TIMESTAMP_FORMAT);
        } catch (Exception e) {
            logger.warn("Не удалось распарсить timestamp: {}", timestamp);
            return null;
        }
    }
    
    /**
     * Форматирует длительность в секундах в читаемый формат
     * 
     * Примеры:
     * - 45 секунд → "45s"
     * - 125 секунд → "2m 5s"
     * - 3665 секунд → "1h 1m 5s"
     * 
     * @param seconds количество секунд
     * @return отформатированная строка
     */
    public static String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            if (remainingSeconds == 0) {
                return minutes + "m";
            } else {
                return minutes + "m " + remainingSeconds + "s";
            }
        } else {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            long remainingSeconds = seconds % 60;
            if (remainingMinutes == 0 && remainingSeconds == 0) {
                return hours + "h";
            } else if (remainingSeconds == 0) {
                return hours + "h " + remainingMinutes + "m";
            } else {
                return hours + "h " + remainingMinutes + "m " + remainingSeconds + "s";
            }
        }
    }
    
    /**
     * Извлекает GC-связанные опции из строки JAVA_TOOL_OPTIONS
     * 
     * @param javaToolOptions строка с JAVA_TOOL_OPTIONS
     * @return строка с GC опциями, разделенными переносами строк, или пустая строка
     */
    public static String extractGcOptions(String javaToolOptions) {
        if (javaToolOptions == null || javaToolOptions.trim().isEmpty()) {
            return "";
        }
        
        Matcher matcher = GC_OPTIONS_PATTERN.matcher(javaToolOptions);
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
     * Очищает имя Docker образа от registry префиксов
     * 
     * Удаляет префиксы:
     * - pcss-prod*:
     * - nexus*:
     * - docker*:
     * 
     * @param image полное имя образа с registry
     * @return очищенное имя образа
     */
    public static String cleanImageName(String image) {
        if (image == null || image.trim().isEmpty()) {
            return image;
        }
        
        // Удаляем registry префиксы до первого / или до :
        // Для pcss-prod.example.com:5000/image:tag -> image:tag
        // Для nexus.example.com/image:tag -> image:tag
        String result = image.replaceAll("^pcss-prod[^/:]*[:/]", "/")
                           .replaceAll("^nexus[^/:]*[:/]", "/")
                           .replaceAll("^docker[^/:]*[:/]", "/");
        
        // Убираем ведущий слэш, если он остался
        if (result.startsWith("/")) {
            result = result.substring(1);
        }
        
        return result;
    }
}

