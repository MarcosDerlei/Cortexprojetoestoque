package com.empresa.estoque.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record ItemUpdateDTO(
        String descricao,

        Long categoriaId,
        Long subcategoriaId,

        String unidade,

        @PositiveOrZero Double custoUnitario,
        @PositiveOrZero Double pontoReposicao,

        Boolean ativo,

        String fornecedor,
        String localizacao,
        String observacao
) {
}
