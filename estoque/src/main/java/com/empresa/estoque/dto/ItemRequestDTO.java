package com.empresa.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ItemRequestDTO(
        @NotBlank String sku,
        @NotBlank String descricao,

        @NotNull Long categoriaId,
        Long subcategoriaId,

        @NotBlank String unidade,
        @PositiveOrZero Double quantidadeInicial,
        @PositiveOrZero Double pontoReposicao,
        @PositiveOrZero Double custoUnitario,

        String fornecedor,
        String localizacao,
        String observacao
) {
}
