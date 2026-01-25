package com.empresa.estoque.dashboardCategorias.dto;

import java.util.List;

public record DashboardCategoriasResponseDTO(
        DashboardResumoDTO resumo,
        List<CategoriaDashboardDTO> categorias
) {}
