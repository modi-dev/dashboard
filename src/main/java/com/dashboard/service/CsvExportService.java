package com.dashboard.service;

import com.dashboard.model.PodInfo;
import com.dashboard.model.Server;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Сервис для экспорта данных в CSV формат
 * 
 * CSV (Comma-Separated Values) - текстовый формат для хранения таблиц.
 * Используется для скачивания данных о серверах и подах в файл.
 * 
 * Пример CSV:
 * ID;Name;URL;Type
 * 1;My Server;https://google.com;OTHER
 * 
 * @Service - говорит Spring, что это сервис с бизнес-логикой
 */
@Service
public class CsvExportService {
    
    // Формат для даты и времени: 2025-10-27 09:00:00
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Разделитель полей - точка с запятой (стандарт для русской версии Excel)
    private static final String CSV_SEPARATOR = ";";
    
    /**
     * Экспортирует список серверов в CSV формат
     * @param servers список серверов для экспорта
     * @return строка в формате CSV
     */
    public String exportServersToCsv(List<Server> servers) {
        StringBuilder csv = new StringBuilder();
        
        // Заголовок CSV
        csv.append("ID").append(CSV_SEPARATOR)
           .append("Name").append(CSV_SEPARATOR)
           .append("URL").append(CSV_SEPARATOR)
           .append("Type").append(CSV_SEPARATOR)
           .append("Status").append(CSV_SEPARATOR)
           .append("Healthcheck").append(CSV_SEPARATOR)
           .append("Last Checked").append(CSV_SEPARATOR)
           .append("Created At").append(CSV_SEPARATOR)
           .append("Updated At\n");
        
        // Данные
        for (Server server : servers) {
            csv.append(escapeCsvValue(String.valueOf(server.getId()))).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(server.getName())).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(server.getUrl())).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(server.getType().getDisplayName())).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(server.getStatus().name())).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(server.getHealthcheck() != null ? server.getHealthcheck() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(server.getLastChecked() != null ? server.getLastChecked().format(DATE_FORMATTER) : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(server.getCreatedAt() != null ? server.getCreatedAt().format(DATE_FORMATTER) : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(server.getUpdatedAt() != null ? server.getUpdatedAt().format(DATE_FORMATTER) : ""));
            csv.append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Экспортирует список подов в CSV формат
     * @param pods список подов для экспорта
     * @return строка в формате CSV
     */
    public String exportPodsToCsv(List<PodInfo> pods) {
        StringBuilder csv = new StringBuilder();
        
        // Заголовок CSV
        csv.append("Name").append(CSV_SEPARATOR)
           .append("Version").append(CSV_SEPARATOR)
           .append("MS Branch").append(CSV_SEPARATOR)
           .append("Config Branch").append(CSV_SEPARATOR)
           .append("GC Options").append(CSV_SEPARATOR)
           .append("Port").append(CSV_SEPARATOR)
           .append("Replicas").append(CSV_SEPARATOR)
           .append("CPU Request").append(CSV_SEPARATOR)
           .append("Memory Request").append(CSV_SEPARATOR)
           .append("Creation Date\n");
        
        // Данные
        for (PodInfo pod : pods) {
            csv.append(escapeCsvValue(pod.getName() != null ? pod.getName() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getVersion() != null ? pod.getVersion() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getMsBranch() != null ? pod.getMsBranch() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getConfigBranch() != null ? pod.getConfigBranch() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getGcOptions() != null ? pod.getGcOptions() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getPort() != null ? String.valueOf(pod.getPort()) : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getReplicas() != null ? String.valueOf(pod.getReplicas()) : "1")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getCpuRequest() != null ? pod.getCpuRequest() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getMemoryRequest() != null ? pod.getMemoryRequest() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getCreationDate() != null ? pod.getCreationDate().format(DATE_FORMATTER) : ""));
            csv.append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Экранирует значение для безопасного использования в CSV
     * 
     * Проблема: если в данных есть точка с запятой или кавычки, CSV сломается.
     * Решение: оборачиваем такие значения в кавычки и удваиваем кавычки внутри.
     * 
     * Пример: значение: My "Special" Server;Test
     *         результат: "My ""Special"" Server;Test"
     * 
     * @param value значение для экранирования
     * @return экранированное значение (безопасное для CSV)
     */
    private String escapeCsvValue(String value) {
        // Если значение пустое - возвращаем пустую строку
        if (value == null) {
            return "";
        }
        
        // Проверяем, содержит ли значение "опасные" символы
        if (value.contains(CSV_SEPARATOR) || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // Заменяем одинарные кавычки на двойные (CSV стандарт)
            value = value.replace("\"", "\"\"");
            // Оборачиваем в кавычки
            return "\"" + value + "\"";
        }
        
        // Если опасных символов нет - возвращаем как есть
        return value;
    }
}
