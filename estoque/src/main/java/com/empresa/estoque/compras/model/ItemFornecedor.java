package com.empresa.estoque.compras.model;

import com.empresa.estoque.model.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_fornecedor",
        uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "fornecedor_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemFornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @PositiveOrZero
    @Column(name = "preco_referencia", nullable = false)
    private BigDecimal precoReferencia = BigDecimal.ZERO;

    @Column(name = "unidade_compra", length = 30)
    private String unidadeCompra; // ex: chapa, pc, barra

    @Column(length = 255)
    private String observacao;

    private boolean ativo = true;
}
