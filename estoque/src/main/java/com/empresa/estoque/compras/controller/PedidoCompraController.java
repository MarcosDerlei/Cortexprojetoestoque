package com.empresa.estoque.compras.controller;

import com.empresa.estoque.compras.dto.AddItemCarrinhoRequestDTO;
import com.empresa.estoque.compras.dto.PedidoCompraResponseDTO;
import com.empresa.estoque.compras.service.PedidoCompraService;
import com.empresa.estoque.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compras/carrinho")
@RequiredArgsConstructor
public class PedidoCompraController {

    private final PedidoCompraService pedidoCompraService;

    @GetMapping
    public PedidoCompraResponseDTO getCarrinho(@AuthenticationPrincipal User user) {
        return pedidoCompraService.obterCarrinho(user);
    }

    @PostMapping("/itens")
    public void adicionarItem(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid AddItemCarrinhoRequestDTO dto
    ) {
        pedidoCompraService.adicionarItemNoCarrinho(user, dto);
    }

    @DeleteMapping("/itens/{itemCarrinhoId}")
    public void removerItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemCarrinhoId
    ) {
        pedidoCompraService.removerItem(user, itemCarrinhoId);
    }

    @PostMapping("/finalizar")
    public void finalizar(@AuthenticationPrincipal User user) {
        pedidoCompraService.finalizar(user);
    }
}
