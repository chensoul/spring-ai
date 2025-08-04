package com.example.kb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 主页控制器
 * 
 * 提供应用首页和基本信息
 */
@Controller
public class HomeController {
    
    /**
     * 应用首页
     */
    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        model.addAttribute("appName", "企业知识库系统");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("currentTime", LocalDateTime.now());
        
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("authorities", authentication.getAuthorities());
        }
        
        return "index";
    }
    
    /**
     * API信息
     */
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = Map.of(
            "application", "Enterprise Knowledge Base",
            "version", "1.0.0",
            "description", "基于Spring AI的企业知识库系统",
            "features", new String[]{
                "RAG智能问答",
                "文档上传处理", 
                "向量化搜索",
                "用户权限管理"
            },
            "timestamp", LocalDateTime.now(),
            "status", "running"
        );
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * API文档重定向
     */
    @GetMapping("/docs")
    public String docs() {
        return "redirect:/swagger-ui.html";
    }
    
    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}