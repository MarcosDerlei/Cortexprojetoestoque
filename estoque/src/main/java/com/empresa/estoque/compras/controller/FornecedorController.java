package com.empresa.estoque.compras.controller;

import com.empresa.estoque.compras.dto.FornecedorRequestDTO;
import com.empresa.estoque.compras.dto.FornecedorResponseDTO;
import com.empresa.estoque.compras.service.FornecedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @GetMapping
    public List<FornecedorResponseDTO> listar(
            @RequestParam(required = false, defaultValue = "true") Boolean somenteAtivos
    ) {
        return fornecedorService.listar(somenteAtivos);
    }

    @GetMapping("/{id}")
    public FornecedorResponseDTO buscarPorId(@PathVariable Long id) {
        return fornecedorService.buscarPorId(id);
    }

    @PostMapping
    public FornecedorResponseDTO criar(@RequestBody @Valid FornecedorRequestDTO dto) {
        return fornecedorService.criar(dto);
    }

    @PutMapping("/{id}")
    public FornecedorResponseDTO atualizar(
            @PathVariable Long id,
            @RequestBody @Valid FornecedorRequestDTO dto
    ) {
        return fornecedorService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        fornecedorService.deletar(id);
    }
}
