package com.dashboard.controller;

import com.dashboard.model.Server;
import com.dashboard.model.PodInfo;
import com.dashboard.service.KubernetesService;
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
    
    /**
     * Главная страница dashboard
     * GET /dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Получаем список серверов
        List<Server> servers = serverRepository.findAll();
        model.addAttribute("servers", servers);
        
        // Пока что без Kubernetes - просто пустой список
        List<PodInfo> pods = new ArrayList<>();
        model.addAttribute("pods", pods);
        
        // Статистика
        model.addAttribute("totalServers", servers.size());
        model.addAttribute("onlineServers", servers.stream().mapToInt(s -> s.getStatus().name().equals("ONLINE") ? 1 : 0).sum());
        model.addAttribute("offlineServers", servers.stream().mapToInt(s -> s.getStatus().name().equals("OFFLINE") ? 1 : 0).sum());
        model.addAttribute("totalPods", pods.size());
        
        return "dashboard";
    }
    
}
