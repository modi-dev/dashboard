package com.dashboard.controller;

import com.dashboard.model.PodInfo;
import com.dashboard.service.KubernetesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        try {
            logger.info("Запрос HTML страницы с информацией о подах");
            List<PodInfo> pods = kubernetesService.getRunningPods();

            // Подсчеты статистики
            int totalReplicas = pods.stream()
                    .mapToInt(p -> p.getReplicas() != null ? p.getReplicas() : 1)
                    .sum();
            int uniquePods = pods.size();
            long withReplicas = pods.stream()
                    .filter(p -> (p.getReplicas() != null ? p.getReplicas() : 1) > 1)
                    .count();

            model.addAttribute("pods", pods);
            model.addAttribute("totalPods", totalReplicas); // всего подов (с учетом реплик)
            model.addAttribute("uniquePods", uniquePods);   // уникальные записи
            model.addAttribute("totalReplicas", totalReplicas);
            model.addAttribute("withReplicas", withReplicas);
            model.addAttribute("kubernetesVersion", kubernetesService.getKubernetesVersion());
            return "pods";
        } catch (Exception e) {
            logger.error("Ошибка при получении HTML страницы с подами: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка при загрузке информации о подах: " + e.getMessage());
            return "pods";
        }
    }
}

