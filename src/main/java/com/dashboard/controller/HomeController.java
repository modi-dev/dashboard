package com.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard/api")
    @ResponseBody
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Version Dashboard API");
        response.put("version", "1.0.0");
        response.put("status", "running");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("servers", "/dashboard/api/servers");
        endpoints.put("health", "/actuator/health");
        endpoints.put("metrics", "/actuator/metrics");
        
        response.put("endpoints", endpoints);
        
        return response;
    }
}

