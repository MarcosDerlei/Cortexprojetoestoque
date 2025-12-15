package com.empresa.estoque.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "subcategorias_item")

public class SubcategoriaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaItem categoria;

    private boolean ativo = true;
}
