package com.empresa.estoque.compras.controller;

import com.empresa.estoque.compras.dto.ItemFornecedorPrecoResponseDTO;
import com.empresa.estoque.compras.dto.ItemFornecedorRequestDTO;
import com.empresa.estoque.compras.dto.ItemFornecedorResponseDTO;
import com.empresa.estoque.compras.dto.ItemFornecedorUpdateDTO;
import com.empresa.estoque.compras.service.ItemFornecedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor

public class ItemFornecedorController {

    private final ItemFornecedorService itemFornecedorService;

    @GetMapping("/item/{itemId}/fornecedores")
    public List<ItemFornecedorPrecoResponseDTO> listarFornecedoresDoItem(@PathVariable Long itemId) {
        return itemFornecedorService.listarFornecedoresDoItem(itemId);
    }

    @PostMapping("/item-fornecedor")
    public ItemFornecedorResponseDTO vincularFornecedorAoItem(
            @RequestBody @Valid ItemFornecedorRequestDTO dto
    ) {
        return itemFornecedorService.vincular(dto);
    }

    @GetMapping("/item-fornecedor")
    public List<ItemFornecedorResponseDTO> listarVinculosDoItem(@RequestParam Long itemId) {
        return itemFornecedorService.listarPorItem(itemId);
    }

    @DeleteMapping("/item-fornecedor/{id}")
    public void deletarVinculo(@PathVariable Long id) {
        itemFornecedorService.deletar(id);
    }

    @PutMapping("/item-fornecedor/{id}")
    public ItemFornecedorResponseDTO atualizarVinculo(
            @PathVariable Long id,
            @RequestBody @Valid ItemFornecedorUpdateDTO dto
    ) {
        return itemFornecedorService.atualizar(id, dto);
    }

}
