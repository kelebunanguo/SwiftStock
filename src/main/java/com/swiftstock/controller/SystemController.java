package com.swiftstock.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system")
public class SystemController {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);
    
    @GetMapping("/settings")
    public String settings(Model model) {
        logger.info("Accessing system settings page");
        return "system/settings";
    }
} 