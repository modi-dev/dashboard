package com.dashboard.controller;

import com.dashboard.model.PodInfo;
import com.dashboard.service.KubernetesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Контроллер для страницы подов
 */
@Controller
public class PodsController {
    
    private static final Logger logger = LoggerFactory.getLogger(PodsController.class);
    
    @Autowired
    private KubernetesService kubernetesService;
    
    /**
     * Страница подов
     * GET /pods
     */
    @GetMapping("/pods")
    public String pods(Model model) {
        // Добавляем информацию об авторизации для Thymeleaf
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                                 !auth.getName().equals("anonymousUser");
        model.addAttribute("isAuthenticated", isAuthenticated);
        try {
            logger.info("Запрос HTML страницы с информацией о подах");
            List<PodInfo> pods = kubernetesService.getRunningPods();

            // Подсчеты статистики
            int totalPods = pods.size();

            model.addAttribute("pods", pods);
            model.addAttribute("totalPods", totalPods); // всего подов
            model.addAttribute("uniqueServices", kubernetesService.countUniqueServices(pods));   // уникальные сервисы (по полю name)
            model.addAttribute("totalReplicas", totalPods);
            model.addAttribute("withReplicas", 0); // больше нет группировки
            model.addAttribute("kubernetesVersion", kubernetesService.getKubernetesVersion());
            return "pods";
        } catch (Exception e) {
            logger.error("Ошибка при получении HTML страницы с подами: {}", e.getMessage(), e);
            // Убеждаемся, что isAuthenticated установлен даже при ошибке
            if (!model.containsAttribute("isAuthenticated")) {
                Authentication authForError = SecurityContextHolder.getContext().getAuthentication();
                boolean isAuth = authForError != null && authForError.isAuthenticated() && 
                               !authForError.getName().equals("anonymousUser");
                model.addAttribute("isAuthenticated", isAuth);
            }
            model.addAttribute("error", "Ошибка при загрузке информации о подах: " + e.getMessage());
            return "pods";
        }
    }
}

