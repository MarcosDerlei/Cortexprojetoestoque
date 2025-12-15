package com.empresa.estoque.controller;

import com.empresa.estoque.dto.EstoqueRequestDTO;
import com.empresa.estoque.dto.EstoqueResponseDTO;
import com.empresa.estoque.model.Estoque;
import com.empresa.estoque.service.EstoqueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    // POST - Cadastrar
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@RequestBody @Valid EstoqueRequestDTO dto) {
        try {
            estoqueService.cadastrarEstoque(dto);
            return ResponseEntity.ok("Estoque cadastrado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET - Listar todos
    @GetMapping
    public ResponseEntity<List<EstoqueResponseDTO>> listarTodos() {
        try {
            return ResponseEntity.ok(estoqueService.listarTodos());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }


    // GET - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(estoqueService.buscarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT - Atualizar
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody @Valid EstoqueRequestDTO dto) {
        try {
            estoqueService.atualizarEstoque(id, dto);
            return ResponseEntity.ok("Estoque atualizado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE - Excluir
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            estoqueService.deletarEstoque(id);
            return ResponseEntity.ok("Estoque removido com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
