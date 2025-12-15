package com.empresa.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record MovimentoRequestDTO(
        @NotBlank String sku,
        @NotBlank String tipo,
        @PositiveOrZero Double quantidade,
        String observacao
) {}

