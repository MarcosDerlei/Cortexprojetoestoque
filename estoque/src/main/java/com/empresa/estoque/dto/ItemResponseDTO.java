package com.empresa.estoque.dto;

import java.time.LocalDateTime;

public record ItemResponseDTO(
        Long id,
        String sku,
        String descricao,

        // Categoria e Subcategoria
        Long categoriaId,
        String categoriaNome,
        Long subcategoriaId,
        String subcategoriaNome,

        // Unidade e estoque
        String unidade,
        Double saldo,
        Double reservado,
        Double pontoReposicao,
        Integer estoqueMinimo,
        Double custoUnitario,


        // Status
        boolean ativo,
        String alerta,

        // Informações adicionais
        String fornecedor,
        String localizacao,
        String observacao,

        // Metadata
        LocalDateTime ultimaAtualizacao
) {}
