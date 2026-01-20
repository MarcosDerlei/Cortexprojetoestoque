package com.empresa.estoque.compras.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AddItemCarrinhoRequestDTO(
        @NotNull Long itemFornecedorId,
        @NotNull @Positive BigDecimal quantidade
) {}