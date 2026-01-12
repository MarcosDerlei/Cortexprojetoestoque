package com.empresa.estoque.dashboardCategorias.controller;

import com.empresa.estoque.dashboardCategorias.dto.CategoriaDashboardDTO;
import com.empresa.estoque.dashboardCategorias.dto.DashboardResumoDTO;
import com.empresa.estoque.dashboardCategorias.service.DashboardCategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/dashboard/categorias")
@RequiredArgsConstructor
public class DashboardCategoriaController {
    private final DashboardCategoriaService dashboardService;

    @GetMapping
    public List<CategoriaDashboardDTO> listar() {
        return dashboardService.listarCategorias();
    }

    @GetMapping("/resumo")
    public DashboardResumoDTO resumo() {
        return dashboardService.gerarResumo();
    }
}
