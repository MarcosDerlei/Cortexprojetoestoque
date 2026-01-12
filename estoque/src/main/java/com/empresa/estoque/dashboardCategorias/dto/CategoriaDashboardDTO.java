package com.empresa.estoque.dashboardCategorias.dto;

import com.empresa.estoque.dashboardCategorias.enums.GiroEstoque;
import com.empresa.estoque.dashboardCategorias.enums.StatusCategoria;

import java.math.BigDecimal;

public record CategoriaDashboardDTO(
        Long id,
        String nome,
        Integer totalItens,
        BigDecimal valorEstoque,
        Integer itensAbaixoMinimo,
        StatusCategoria status,
        GiroEstoque giro,
        BigDecimal variacao30d
) {
}
