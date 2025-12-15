package com.empresa.estoque.controller;

import com.empresa.estoque.dto.SubcategoriaItemRequestDTO;
import com.empresa.estoque.dto.SubcategoriaItemResponseDTO;
import com.empresa.estoque.service.SubcategoriaItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subcategorias")
public class SubcategoriaItemController {

    @Autowired
    private SubcategoriaItemService service;

    @PostMapping
    public ResponseEntity<SubcategoriaItemResponseDTO> criar(@RequestBody SubcategoriaItemRequestDTO dto) {
        return ResponseEntity.ok(service.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<SubcategoriaItemResponseDTO>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<SubcategoriaItemResponseDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(service.listarPorCategoria(categoriaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubcategoriaItemResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody SubcategoriaItemRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
