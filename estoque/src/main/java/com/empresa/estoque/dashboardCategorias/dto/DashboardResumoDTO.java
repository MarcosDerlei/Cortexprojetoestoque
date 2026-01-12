package com.empresa.estoque.dashboardCategorias.dto;

import java.math.BigDecimal;

public record DashboardResumoDTO(
        Integer totalCategorias,
        Integer categoriasCriticas,
        BigDecimal valorTotalEstoque,
        String maiorConsumo30d
) {
}
