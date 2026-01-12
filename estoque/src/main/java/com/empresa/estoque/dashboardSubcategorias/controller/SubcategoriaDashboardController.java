package com.empresa.estoque.dashboardSubcategorias.controller;

import com.empresa.estoque.dashboardSubcategorias.dto.SubcategoriaDashboardResponseDTO;
import com.empresa.estoque.dashboardSubcategorias.service.SubcategoriaDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/dashboard/categorias")
@RequiredArgsConstructor
public class SubcategoriaDashboardController {

    private final SubcategoriaDashboardService subcategoriaDashboardService;

    @GetMapping("/{categoriaId}/subcategorias")
    public SubcategoriaDashboardResponseDTO dashboard(
            @PathVariable Long categoriaId
    ) {
        return subcategoriaDashboardService.getDashboard(categoriaId);
    }
}
