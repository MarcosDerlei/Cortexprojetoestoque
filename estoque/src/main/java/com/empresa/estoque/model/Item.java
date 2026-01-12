package com.empresa.estoque.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "itens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String descricao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaItem categoria;

    @NotBlank
    private String unidade;

    @PositiveOrZero
    private Double custoUnitario;

    private boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "subcategoria_id")
    private SubcategoriaItem subcategoria;
    private String fornecedor;
    private String localizacao;
    private String observacao;
    private LocalDateTime ultimaAtualizacao;

    @PrePersist
    @PreUpdate
    private void atualizarData() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    @PositiveOrZero
    @Column(name = "estoque_minimo", nullable = false)
    private Integer estoqueMinimo = 0;

}
