package com.empresa.estoque.compras.controller;

import com.empresa.estoque.compras.dto.AddItemCarrinhoRequestDTO;
import com.empresa.estoque.compras.dto.PedidoCompraResponseDTO;
import com.empresa.estoque.compras.service.PedidoCompraService;
import com.empresa.estoque.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    // ========================================================================
    // ✅ NOVOS ENDPOINTS: Gerenciamento de status por fornecedor
    // ========================================================================

    /**
     * Marca todos os itens de um fornecedor como ENVIADO
     * POST /compras/carrinho/enviar/{fornecedorId}
     */
    @PostMapping("/enviar/{fornecedorId}")
    public ResponseEntity<Map<String, String>> marcarComoEnviado(
            @AuthenticationPrincipal User user,
            @PathVariable Long fornecedorId
    ) {
        pedidoCompraService.marcarComoEnviado(user, fornecedorId);
        return ResponseEntity.ok(Map.of(
                "message", "Pedido marcado como enviado",
                "status", "ENVIADO"
        ));
    }

    /**
     * Volta os itens de um fornecedor para RASCUNHO (cancela envio)
     * POST /compras/carrinho/cancelar-envio/{fornecedorId}
     */
    @PostMapping("/cancelar-envio/{fornecedorId}")
    public ResponseEntity<Map<String, String>> cancelarEnvio(
            @AuthenticationPrincipal User user,
            @PathVariable Long fornecedorId
    ) {
        pedidoCompraService.cancelarEnvio(user, fornecedorId);
        return ResponseEntity.ok(Map.of(
                "message", "Pedido voltou para rascunho",
                "status", "RASCUNHO"
        ));
    }

    /**
     * Confirma o recebimento do pedido e REMOVE os itens do fornecedor do carrinho
     * POST /compras/carrinho/confirmar/{fornecedorId}
     */
    @PostMapping("/confirmar/{fornecedorId}")
    public ResponseEntity<Map<String, String>> confirmarPedido(
            @AuthenticationPrincipal User user,
            @PathVariable Long fornecedorId
    ) {
        pedidoCompraService.confirmarPedido(user, fornecedorId);
        return ResponseEntity.ok(Map.of(
                "message", "Pedido confirmado e removido do carrinho",
                "status", "CONFIRMADO"
        ));
    }
}