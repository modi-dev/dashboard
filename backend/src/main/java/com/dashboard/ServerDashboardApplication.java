package com.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServerDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerDashboardApplication.class, args);
    }
}

