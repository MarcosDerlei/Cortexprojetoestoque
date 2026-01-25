package com.empresa.estoque.dashboardCategorias.controller;

import com.empresa.estoque.dashboardCategorias.dto.DashboardCategoriasResponseDTO;
import com.empresa.estoque.dashboardCategorias.service.DashboardCategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard/categorias")
@RequiredArgsConstructor
public class DashboardCategoriaController {

    private final DashboardCategoriaService dashboardService;

    @GetMapping
    public DashboardCategoriasResponseDTO dashboard() {
        return dashboardService.carregarDashboard();
    }
}
