package com.empresa.estoque.dashboardSubcategorias.dto;

import java.util.List;

public record SubcategoriaDashboardResponseDTO(
        SubcategoriaDashboardResumoDTO resumo,
        List<SubcategoriaDashboardDTO> subcategorias
) {
}
