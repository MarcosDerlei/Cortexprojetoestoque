package com.empresa.estoque.controller;

import com.empresa.estoque.dto.MovimentoRequestDTO;
import com.empresa.estoque.dto.MovimentoResponseDTO;
import com.empresa.estoque.service.MovimentoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movimentos")
public class MovimentoController {

    private final MovimentoService movimentoService;

    public MovimentoController(MovimentoService movimentoService) {
        this.movimentoService = movimentoService;
    }

    @PostMapping
    public MovimentoResponseDTO registrar(@RequestBody @Valid MovimentoRequestDTO dto) {
        return movimentoService.registrar(dto);
    }

    @GetMapping("/item/{sku}")
    public List<MovimentoResponseDTO> listarPorItem(@PathVariable String sku) {
        return movimentoService.listarPorItem(sku);
    }

    @GetMapping
    public List<MovimentoResponseDTO> listarTodos() {
        return movimentoService.listarTodos();
    }

    @GetMapping("/{id}")
    public MovimentoResponseDTO buscarPorId(@PathVariable Long id) {
        return movimentoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public MovimentoResponseDTO atualizar(@PathVariable Long id,
                                          @RequestBody @Valid MovimentoRequestDTO dto) {
        return movimentoService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        movimentoService.deletar(id);
    }
}

