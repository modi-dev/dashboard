package com.dashboard.controller;

import com.dashboard.config.DashboardPaths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Контроллер для страницы логина
 */
@Controller
@RequestMapping("/dashboard")
public class LoginController {

    @Value("${security.enabled:true}")
    private boolean securityEnabled;

    @GetMapping("/login")
    public String login(@RequestParam(value = "redirect", required = false) String redirect, Model model) {
        addLoginModelAttributes(model, redirect, false, null);
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError(
            @RequestParam(value = "redirect", required = false) String redirect,
            Model model) {
        addLoginModelAttributes(model, redirect, true, "Неверный логин или пароль");
        return "login";
    }

    private void addLoginModelAttributes(Model model, String redirect, boolean error, String errorMessage) {
        model.addAttribute("securityEnabled", securityEnabled);
        model.addAttribute("skipUrl", resolveSkipUrl(redirect));
        if (error) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", errorMessage);
        }
    }

    private String resolveSkipUrl(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return DashboardPaths.BASE;
        }
        String path = redirect.startsWith("/") ? redirect : "/" + redirect;
        if (!path.startsWith(DashboardPaths.BASE + "/") && !path.equals(DashboardPaths.BASE)) {
            return DashboardPaths.BASE;
        }
        return path;
    }
}

