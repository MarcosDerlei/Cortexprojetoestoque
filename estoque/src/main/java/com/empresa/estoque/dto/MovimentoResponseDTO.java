package com.empresa.estoque.dto;

import java.time.LocalDateTime;

public record MovimentoResponseDTO(
        Long id,
        String sku,
        String tipo,
        Double quantidade,
        LocalDateTime dataMovimento,
        String observacao
) {
}
