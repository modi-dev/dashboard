package com.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Главный класс приложения Server Dashboard
 * 
 * Это точка входа в приложение - отсюда все начинается.
 * Spring Boot автоматически настраивает все необходимые компоненты.
 * 
 * Аннотации:
 * @SpringBootApplication - говорит Spring, что это главный класс приложения
 *                          (включает автоконфигурацию, сканирование компонентов и конфигурацию)
 * @EnableScheduling - включает возможность запускать задачи по расписанию
 *                     (например, проверка серверов каждые 30 секунд)
 */
@SpringBootApplication
@EnableScheduling
public class ServerDashboardApplication {

    /**
     * Главный метод - отсюда запускается приложение
     * 
     * @param args - аргументы командной строки (можно передать настройки при запуске)
     */
    public static void main(String[] args) {
        // Запускаем Spring Boot приложение
        // Spring сам найдет все контроллеры, сервисы, репозитории и настроит их
        SpringApplication.run(ServerDashboardApplication.class, args);
    }
}

