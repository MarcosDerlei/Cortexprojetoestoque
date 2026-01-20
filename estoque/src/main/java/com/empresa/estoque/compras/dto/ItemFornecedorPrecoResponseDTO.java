package com.empresa.estoque.compras.dto;

import java.math.BigDecimal;

public record ItemFornecedorPrecoResponseDTO(
        Long itemFornecedorId,
        Long fornecedorId,
        String fornecedorNome,
        String fornecedorWhatsapp,
        BigDecimal precoReferencia,
        String unidadeCompra,
        String observacao
) {}
