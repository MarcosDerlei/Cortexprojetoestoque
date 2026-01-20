package com.empresa.estoque.compras.dto;

import com.empresa.estoque.compras.model.StatusPedidoCompra;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

public record PedidoCompraResponseDTO(
        Long id,
        StatusPedidoCompra status,
        LocalDateTime dataCriacao,
        List<PedidoCompraItemResponseDTO> itens,
        BigDecimal totalEstimado
) {}