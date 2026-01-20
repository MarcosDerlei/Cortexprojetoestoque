package com.empresa.estoque.compras.dto;

import java.math.BigDecimal;

public record PedidoCompraItemResponseDTO(
        Long id,
        Long itemId,
        String sku,
        String descricao,
        String unidadeItem,

        Long fornecedorId,
        String fornecedorNome,
        String fornecedorWhatsapp,

        BigDecimal quantidade,
        BigDecimal preco,
        BigDecimal subtotal
) {}