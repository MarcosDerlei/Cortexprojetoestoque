package com.empresa.estoque.dto;

public record SubcategoriaItemRequestDTO(
        String nome,
        Long categoriaId,
        Boolean ativo
) {
}
