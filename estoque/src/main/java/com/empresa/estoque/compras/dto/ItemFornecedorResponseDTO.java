package com.empresa.estoque.compras.dto;

import java.math.BigDecimal;

public record ItemFornecedorResponseDTO(
        Long id,

        Long itemId,
        String sku,
        String descricao,

        Long fornecedorId,
        String fornecedorNome,
        String fornecedorWhatsapp,

        BigDecimal precoReferencia,
        String unidadeCompra,
        boolean ativo
) {}