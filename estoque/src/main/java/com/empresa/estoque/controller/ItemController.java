package com.empresa.estoque.controller;

import com.empresa.estoque.dto.*;
import com.empresa.estoque.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/itens")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/subcategoria/{id}")
    public ResponseEntity<List<ItemResponseDTO>> listarPorSubcategoria(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.listarPorSubcategoria(id));
    }


    // LISTAR TODOS OU POR CATEGORIA (ex: /itens?categoriaId=1)
    @GetMapping
    public List<ItemResponseDTO> listar(@RequestParam(required = false) Long categoriaId) {
        if (categoriaId != null) {
            return itemService.listarPorCategoria(categoriaId);
        }
        return itemService.listar();
    }

    //  BUSCAR POR DESCRIÇÃO OU SKU (ex: /itens/buscar/{termo})
    @GetMapping("/buscar/{termo}")
    public List<ItemResponseDTO> buscar(@PathVariable String termo) {
        return itemService.buscar(termo);
    }

    //  DETALHAR POR SKU (ex: /itens/sku/DOBR-INX-35)
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ItemResponseDTO> detalhar(@PathVariable String sku) {
        return ResponseEntity.ok(itemService.detalhar(sku));
    }

    //  CRIAR NOVO ITEM
    @PostMapping
    public ItemResponseDTO criar(@RequestBody @Valid ItemRequestDTO dto) {
        return itemService.criar(dto);
    }

    //  ATUALIZAR ITEM EXISTENTE
    @PutMapping("/{sku}")
    public ResponseEntity<ItemResponseDTO> atualizar(@PathVariable String sku,
                                                     @RequestBody @Valid ItemUpdateDTO dto) {
        ItemResponseDTO atualizado = itemService.atualizar(sku, dto);
        return ResponseEntity.ok(atualizado);
    }

    //  DESATIVAR ITEM
    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> deletar(@PathVariable String sku) {
        itemService.desativar(sku);
        return ResponseEntity.noContent().build();
    }
}
