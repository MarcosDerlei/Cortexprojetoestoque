package com.empresa.estoque.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
@Table(name = "categorias_item")

public class CategoriaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(length = 20, unique = true)
    private String codigo;

    @Column(length = 255)
    private String descricao;

    private boolean ativo = true;

    public boolean isAtivo() {
        return ativo;
    }
}
