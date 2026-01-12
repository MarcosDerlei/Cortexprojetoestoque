package com.empresa.estoque.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "API Estoque online ✅";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
