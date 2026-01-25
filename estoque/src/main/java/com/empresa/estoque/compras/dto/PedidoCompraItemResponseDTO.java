package com.empresa.estoque.compras.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        BigDecimal subtotal,

        // NOVO: Status de envio do item
        String statusEnvio,      // "RASCUNHO" ou "ENVIADO"
        LocalDateTime dataEnvio  // null se não enviado
) {}