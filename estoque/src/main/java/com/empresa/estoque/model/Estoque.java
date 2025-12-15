package com.empresa.estoque.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estoques", uniqueConstraints = {@UniqueConstraint(columnNames = {"item_id", "unidade"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private String unidade;

    @PositiveOrZero
    @Column(nullable = false)
    private Double saldo = 0.0;

    @PositiveOrZero
    @Column(nullable = false)
    private Double reservado = 0.0;

    @PositiveOrZero
    private Double pontoReposicao = 0.0;
}
