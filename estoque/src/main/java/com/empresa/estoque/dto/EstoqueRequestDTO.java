package com.empresa.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record EstoqueRequestDTO(
        @NotBlank String sku,
        @NotBlank String unidade,
        @PositiveOrZero Double saldo,
        @PositiveOrZero Double reservado,
        @PositiveOrZero Double pontoReposicao
) {
}
