package com.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для страницы логина
 */
@Controller
@RequestMapping("/dashboard")
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

