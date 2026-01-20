package com.empresa.estoque.compras.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido_compra_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoCompraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_compra_id", nullable = false)
    private PedidoCompra pedidoCompra;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_fornecedor_id", nullable = false)
    private ItemFornecedor itemFornecedor;

    @Positive
    @Column(nullable = false)
    private BigDecimal quantidade;

    @Column(name = "preco_no_momento", nullable = false)
    private BigDecimal precoNoMomento = BigDecimal.ZERO;
}
