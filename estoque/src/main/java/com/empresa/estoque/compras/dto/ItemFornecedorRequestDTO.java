package com.empresa.estoque.compras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ItemFornecedorRequestDTO(
        @NotNull(message = "itemId é obrigatório")
        Long itemId,

        @NotNull(message = "fornecedorId é obrigatório")
        Long fornecedorId,

        @NotNull(message = "precoReferencia é obrigatório")
        BigDecimal precoReferencia,

        @NotBlank(message = "unidadeCompra é obrigatória")
        String unidadeCompra,

        Boolean ativo
) {
}
