package com.empresa.estoque.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentos_estoque")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MovimentoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimento tipo;

    @Positive
    @Column(nullable = false)
    private Double quantidade;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dataMovimento;

    @Column(length = 255)
    private String observacao;
}
