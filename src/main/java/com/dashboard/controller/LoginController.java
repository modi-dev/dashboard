package com.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для страницы логина
 */
@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("error", true);
        model.addAttribute("errorMessage", "Неверный логин или пароль");
        return "login";
    }
}

