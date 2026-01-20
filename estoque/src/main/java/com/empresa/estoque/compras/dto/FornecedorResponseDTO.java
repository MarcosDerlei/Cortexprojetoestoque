package com.empresa.estoque.compras.dto;

public record FornecedorResponseDTO(
        Long id,
        String nome,
        String whatsapp,
        boolean ativo
) {}