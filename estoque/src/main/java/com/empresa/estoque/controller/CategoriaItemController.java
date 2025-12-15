package com.empresa.estoque.controller;

import com.empresa.estoque.model.CategoriaItem;
import com.empresa.estoque.service.CategoriaItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaItemController {

    private final CategoriaItemService service;

    public CategoriaItemController(CategoriaItemService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CategoriaItem> criar(@RequestBody @Valid CategoriaItem categoria) {
        return ResponseEntity.ok(service.salvar(categoria));
    }

    @GetMapping
    public List<CategoriaItem> listar() {
        return service.listarAtivos(); // ou todos
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaItem> atualizar(@PathVariable Long id,
                                                   @RequestBody @Valid CategoriaItem categoriaAtualizada) {
        return ResponseEntity.ok(service.atualizar(id, categoriaAtualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
