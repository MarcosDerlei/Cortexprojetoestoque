package com.empresa.estoque.compras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequestDTO(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 20) String whatsapp,
        Boolean ativo
) {}