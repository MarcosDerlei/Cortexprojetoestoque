package com.empresa.estoque.dashboardCategorias.dto.projection;

import java.math.BigDecimal;

public record CategoriaBigDecimalDTO(
        Long categoriaId,
        BigDecimal total
) {}
