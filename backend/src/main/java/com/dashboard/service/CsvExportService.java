package com.dashboard.service;

import com.dashboard.model.PodInfo;
import com.dashboard.model.Server;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Сервис для экспорта данных в CSV формат
 */
@Service
public class CsvExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_SEPARATOR = ";"; // Разделитель для CSV (точка с запятой для Excel)
    
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
           .append("Port").append(CSV_SEPARATOR)
           .append("CPU Request").append(CSV_SEPARATOR)
           .append("Memory Request").append(CSV_SEPARATOR)
           .append("MS Branch").append(CSV_SEPARATOR)
           .append("Config Branch").append(CSV_SEPARATOR)
           .append("GC Options").append(CSV_SEPARATOR)
           .append("Creation Date\n");
        
        // Данные
        for (PodInfo pod : pods) {
            csv.append(escapeCsvValue(pod.getName() != null ? pod.getName() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getVersion() != null ? pod.getVersion() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getPort() != null ? String.valueOf(pod.getPort()) : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getCpuRequest() != null ? pod.getCpuRequest() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getMemoryRequest() != null ? pod.getMemoryRequest() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getMsBranch() != null ? pod.getMsBranch() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getConfigBranch() != null ? pod.getConfigBranch() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getGcOptions() != null ? pod.getGcOptions() : "")).append(CSV_SEPARATOR);
            csv.append(escapeCsvValue(pod.getCreationDate() != null ? pod.getCreationDate().format(DATE_FORMATTER) : ""));
            csv.append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Экранирует значение для безопасного использования в CSV
     * @param value значение для экранирования
     * @return экранированное значение
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // Если значение содержит разделитель, кавычки или перевод строки, оборачиваем в кавычки
        if (value.contains(CSV_SEPARATOR) || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // Экранируем кавычки удвоением
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        
        return value;
    }
}
