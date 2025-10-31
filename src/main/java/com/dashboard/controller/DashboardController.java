package com.dashboard.controller;

import com.dashboard.model.Server;
import com.dashboard.model.PodInfo;
import com.dashboard.service.KubernetesService;
import com.dashboard.service.ServerVersionService;
import com.dashboard.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Контроллер для отображения страниц Server-Side Rendering (SSR)
 * Использует Thymeleaf для рендеринга HTML
 */
@Controller
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    @Autowired
    private ServerRepository serverRepository;
    
    @Autowired
    private KubernetesService kubernetesService;
    
    @Autowired
    private ServerVersionService serverVersionService;
    
    /**
     * Добавляет статистику серверов в модель
     * 
     * @param model модель для добавления атрибутов
     * @param servers список серверов для подсчета статистики
     */
    private void addServerStatistics(Model model, List<Server> servers) {
        model.addAttribute("totalServers", servers.size());
        model.addAttribute("onlineServers", servers.stream()
                .mapToInt(s -> s.getStatus().name().equals("ONLINE") ? 1 : 0)
                .sum());
        model.addAttribute("offlineServers", servers.stream()
                .mapToInt(s -> s.getStatus().name().equals("OFFLINE") ? 1 : 0)
                .sum());
    }
    
    /**
     * Устанавливает пустые значения статистики серверов в случае ошибки
     * 
     * @param model модель для добавления атрибутов
     */
    private void setEmptyServerStatistics(Model model) {
        model.addAttribute("totalServers", 0);
        model.addAttribute("onlineServers", 0);
        model.addAttribute("offlineServers", 0);
    }
    
    
    /**
     * Главная страница dashboard
     * GET /
     */
    @GetMapping("/")
    public String index(Model model) {
        try {
            // Получаем список серверов
            List<Server> servers = serverRepository.findAllOrderByCreatedAtDesc();
            
            // Получаем версии для всех серверов
            serverVersionService.updateServerVersionsIfNeeded(servers);
            
            model.addAttribute("servers", servers);
            
            // Получаем информацию о подах
            List<PodInfo> pods = kubernetesService.getRunningPods();
            model.addAttribute("pods", pods);
            
            // Статистика серверов
            addServerStatistics(model, servers);
            
            // Статистика подов
            model.addAttribute("totalPods", pods.size());
            
            // Подсчет уникальных сервисов
            model.addAttribute("uniqueServices", kubernetesService.countUniqueServices(pods));
            
            // Версия Kubernetes
            model.addAttribute("kubernetesVersion", kubernetesService.getKubernetesVersion());
            
            logger.info("Главная страница загружена: {} серверов, {} подов", servers.size(), pods.size());
            
        } catch (Exception e) {
            logger.error("Ошибка при загрузке главной страницы: {}", e.getMessage(), e);
            // В случае ошибки показываем пустые данные
            model.addAttribute("servers", new ArrayList<>());
            model.addAttribute("pods", new ArrayList<>());
            setEmptyServerStatistics(model);
            model.addAttribute("totalPods", 0);
            model.addAttribute("uniqueServices", 0);
            model.addAttribute("error", "Ошибка при загрузке данных: " + e.getMessage());
        }
        
        return "index";
    }
    
    /**
     * Страница dashboard (альтернативный маршрут)
     * GET /dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return index(model); // Перенаправляем на главную страницу
    }
    
    /**
     * Страница серверов
     * GET /servers
     */
    @GetMapping("/servers")
    public String servers(Model model) {
        try {
            // Получаем список серверов
            List<Server> servers = serverRepository.findAllOrderByCreatedAtDesc();
            
            // Получаем версии для всех серверов
            serverVersionService.updateServerVersionsIfNeeded(servers);
            
            model.addAttribute("servers", servers);
            
            // Статистика
            addServerStatistics(model, servers);
            
            logger.info("Страница серверов загружена: {} серверов", servers.size());
            
        } catch (Exception e) {
            logger.error("Ошибка при загрузке страницы серверов: {}", e.getMessage(), e);
            model.addAttribute("servers", new ArrayList<>());
            setEmptyServerStatistics(model);
            model.addAttribute("error", "Ошибка при загрузке данных: " + e.getMessage());
        }
        
        return "servers";
    }
    
}
