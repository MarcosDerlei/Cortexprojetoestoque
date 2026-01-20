package com.empresa.estoque.compras.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "fornecedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120, unique = true)
    private String nome;

    // Formato: 5551999999999
    @NotBlank
    @Column(nullable = false, length = 20)
    private String whatsapp;

    private boolean ativo = true;
}
