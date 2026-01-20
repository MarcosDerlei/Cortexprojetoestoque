package com.empresa.estoque.compras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ItemFornecedorUpdateDTO(
        @NotNull(message = "precoReferencia é obrigatório")
        BigDecimal precoReferencia,

        @NotBlank(message = "unidadeCompra é obrigatória")
        String unidadeCompra,

        @NotNull(message = "ativo é obrigatório")
        Boolean ativo
) {}
