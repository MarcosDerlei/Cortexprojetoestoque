package com.empresa.estoque.dto;

import com.empresa.estoque.model.Estoque;

public record EstoqueResponseDTO(
        Long id,
        String sku,
        String unidade,
        Double saldo,
        Double reservado,
        Double pontoReposicao
) {
    public static EstoqueResponseDTO from(Estoque e) {
        return new EstoqueResponseDTO(
                e.getId(),
                e.getItem().getSku(),
                e.getUnidade(),
                e.getSaldo(),
                e.getReservado(),
                e.getPontoReposicao()
        );
    }
}
