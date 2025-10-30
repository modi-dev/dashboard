package com.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Простой тестовый контроллер для проверки работы приложения
 */
@Controller
public class TestController {

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "Application is working! Time: " + java.time.LocalDateTime.now();
    }
}

