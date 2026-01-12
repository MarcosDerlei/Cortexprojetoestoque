package com.empresa.estoque.dashboardSubcategorias.dto;

import com.empresa.estoque.dashboardCategorias.enums.GiroEstoque;
import com.empresa.estoque.dashboardCategorias.enums.StatusCategoria;

import java.math.BigDecimal;

public record SubcategoriaDashboardDTO(
        Long subcategoriaId,
        String nome,
        Integer totalItens,
        Integer itensAbaixoMinimo,
        BigDecimal valorEstoque,
        StatusCategoria status,
        GiroEstoque giro
) {
}
