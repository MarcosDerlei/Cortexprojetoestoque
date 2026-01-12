package com.empresa.estoque.dashboardSubcategorias.dto;

import java.math.BigDecimal;

public record SubcategoriaDashboardResumoDTO(
        Integer totalItens,
        Integer subcategoriasCriticas,
        BigDecimal valorTotalEstoque,
        Integer subcategoriasAbaixoMinimo
) {
}
