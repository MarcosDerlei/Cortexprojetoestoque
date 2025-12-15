package com.empresa.estoque.dto;

public record SubcategoriaItemResponseDTO(
        Long id,
        String nome,
        Long categoriaId,
        boolean ativo
) {
}
