package com.empresa.estoque.dashboardSubcategorias.dto.projection;

import java.math.BigDecimal;

public record SubcategoriaBigDecimalDTO(
        Long subcategoriaId,
        BigDecimal valor
) {}
