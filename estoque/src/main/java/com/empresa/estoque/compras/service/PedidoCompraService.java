package com.empresa.estoque.compras.service;

import com.empresa.estoque.compras.dto.AddItemCarrinhoRequestDTO;
import com.empresa.estoque.compras.dto.PedidoCompraItemResponseDTO;
import com.empresa.estoque.compras.dto.PedidoCompraResponseDTO;
import com.empresa.estoque.compras.model.*;
import com.empresa.estoque.compras.repository.ItemFornecedorRepository;
import com.empresa.estoque.compras.repository.PedidoCompraItemRepository;
import com.empresa.estoque.compras.repository.PedidoCompraRepository;
import com.empresa.estoque.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoCompraService {

    private final PedidoCompraRepository pedidoCompraRepository;
    private final PedidoCompraItemRepository pedidoCompraItemRepository;
    private final ItemFornecedorRepository itemFornecedorRepository;

    @Transactional
    public PedidoCompra obterOuCriarCarrinho(User user) {
        return pedidoCompraRepository
                .findByUserIdAndStatus(user.getId(), StatusPedidoCompra.RASCUNHO)
                .orElseGet(() -> pedidoCompraRepository.save(
                        PedidoCompra.builder()
                                .user(user)
                                .status(StatusPedidoCompra.RASCUNHO)
                                .dataCriacao(LocalDateTime.now())
                                .build()
                ));
    }

    // ✅ GET /compras/carrinho
    @Transactional(readOnly = true)
    public PedidoCompraResponseDTO obterCarrinho(User user) {

        PedidoCompra carrinho = obterOuCriarCarrinho(user);

        List<PedidoCompraItem> itens = pedidoCompraItemRepository.findByPedidoCompraId(carrinho.getId());

        List<PedidoCompraItemResponseDTO> itensDTO = itens.stream()
                .map(i -> {

                    var itemFornecedor = i.getItemFornecedor();
                    var item = itemFornecedor.getItem();
                    var fornecedor = itemFornecedor.getFornecedor();

                    BigDecimal quantidade = i.getQuantidade()
                            .setScale(2, RoundingMode.HALF_UP);

                    BigDecimal preco = i.getPrecoNoMomento()
                            .setScale(2, RoundingMode.HALF_UP);

                    BigDecimal subtotal = preco.multiply(quantidade)
                            .setScale(2, RoundingMode.HALF_UP);

                    return new PedidoCompraItemResponseDTO(
                            i.getId(),
                            item.getId(),
                            item.getSku(),
                            item.getDescricao(),
                            item.getUnidade(),

                            fornecedor.getId(),
                            fornecedor.getNome(),
                            fornecedor.getWhatsapp(),

                            quantidade,
                            preco,
                            subtotal,

                            // ✅ NOVO: status de envio
                            i.getStatusEnvio(),
                            i.getDataEnvio()
                    );
                })
                .toList();

        BigDecimal totalEstimado = itensDTO.stream()
                .map(PedidoCompraItemResponseDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return new PedidoCompraResponseDTO(
                carrinho.getId(),
                carrinho.getStatus(),
                carrinho.getDataCriacao(),
                itensDTO,
                totalEstimado
        );
    }


    @Transactional
    public void adicionarItemNoCarrinho(User user, AddItemCarrinhoRequestDTO dto) {

        PedidoCompra carrinho = obterOuCriarCarrinho(user);

        ItemFornecedor itemFornecedor = itemFornecedorRepository.findById(dto.itemFornecedorId())
                .orElseThrow(() -> new RuntimeException("ItemFornecedor não encontrado"));

        BigDecimal quantidade = dto.quantidade()
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal preco = itemFornecedor.getPrecoReferencia()
                .setScale(2, RoundingMode.HALF_UP);

        var existenteOpt = pedidoCompraItemRepository
                .findByPedidoCompraIdAndItemFornecedorId(carrinho.getId(), itemFornecedor.getId());

        if (existenteOpt.isPresent()) {
            PedidoCompraItem existente = existenteOpt.get();

            BigDecimal novaQuantidade = existente.getQuantidade()
                    .add(quantidade)
                    .setScale(2, RoundingMode.HALF_UP);

            existente.setQuantidade(novaQuantidade);
            existente.setPrecoNoMomento(preco);

            pedidoCompraItemRepository.save(existente);
            return;
        }

        PedidoCompraItem novo = PedidoCompraItem.builder()
                .pedidoCompra(carrinho)
                .itemFornecedor(itemFornecedor)
                .quantidade(quantidade)
                .precoNoMomento(preco)
                .dataEnvio(null) // ✅ Inicia como RASCUNHO
                .build();

        pedidoCompraItemRepository.save(novo);
    }

    @Transactional(readOnly = true)
    public List<PedidoCompraItem> listarItens(Long pedidoCompraId) {
        return pedidoCompraItemRepository.findByPedidoCompraId(pedidoCompraId);
    }

    @Transactional
    public void removerItem(User user, Long itemCarrinhoId) {
        PedidoCompra carrinho = obterOuCriarCarrinho(user);

        PedidoCompraItem item = pedidoCompraItemRepository.findById(itemCarrinhoId)
                .orElseThrow(() -> new RuntimeException("Item do carrinho não encontrado"));

        if (!item.getPedidoCompra().getId().equals(carrinho.getId())) {
            throw new RuntimeException("Item não pertence ao carrinho do usuário");
        }

        pedidoCompraItemRepository.delete(item);
    }

    @Transactional
    public void finalizar(User user) {
        PedidoCompra carrinho = obterOuCriarCarrinho(user);

        if (pedidoCompraItemRepository.findByPedidoCompraId(carrinho.getId()).isEmpty()) {
            throw new RuntimeException("Carrinho vazio. Adicione itens antes de finalizar.");
        }

        carrinho.setStatus(StatusPedidoCompra.ENVIADO);
        carrinho.setDataEnvio(LocalDateTime.now());
        pedidoCompraRepository.save(carrinho);
    }

    // ========================================================================
    // ✅ NOVOS MÉTODOS: Gerenciamento de status por fornecedor
    // ========================================================================

    /**
     * Marca todos os itens de um fornecedor como ENVIADO
     * POST /compras/carrinho/enviar/{fornecedorId}
     */
    @Transactional
    public void marcarComoEnviado(User user, Long fornecedorId) {
        PedidoCompra carrinho = obterOuCriarCarrinho(user);

        List<PedidoCompraItem> itens = pedidoCompraItemRepository.findByPedidoCompraId(carrinho.getId());

        List<PedidoCompraItem> itensFornecedor = itens.stream()
                .filter(i -> i.getItemFornecedor().getFornecedor().getId().equals(fornecedorId))
                .toList();

        if (itensFornecedor.isEmpty()) {
            throw new RuntimeException("Nenhum item encontrado para este fornecedor");
        }

        LocalDateTime agora = LocalDateTime.now();

        for (PedidoCompraItem item : itensFornecedor) {
            item.setDataEnvio(agora);
            pedidoCompraItemRepository.save(item);
        }
    }

    /**
     * Volta os itens de um fornecedor para RASCUNHO (cancela envio)
     * POST /compras/carrinho/cancelar-envio/{fornecedorId}
     */
    @Transactional
    public void cancelarEnvio(User user, Long fornecedorId) {
        PedidoCompra carrinho = obterOuCriarCarrinho(user);

        List<PedidoCompraItem> itens = pedidoCompraItemRepository.findByPedidoCompraId(carrinho.getId());

        List<PedidoCompraItem> itensFornecedor = itens.stream()
                .filter(i -> i.getItemFornecedor().getFornecedor().getId().equals(fornecedorId))
                .toList();

        if (itensFornecedor.isEmpty()) {
            throw new RuntimeException("Nenhum item encontrado para este fornecedor");
        }

        for (PedidoCompraItem item : itensFornecedor) {
            item.setDataEnvio(null); // Volta para RASCUNHO
            pedidoCompraItemRepository.save(item);
        }
    }

    /**
     * Confirma o recebimento do pedido e REMOVE os itens do fornecedor do carrinho
     * POST /compras/carrinho/confirmar/{fornecedorId}
     */
    @Transactional
    public void confirmarPedido(User user, Long fornecedorId) {
        PedidoCompra carrinho = obterOuCriarCarrinho(user);

        List<PedidoCompraItem> itens = pedidoCompraItemRepository.findByPedidoCompraId(carrinho.getId());

        List<PedidoCompraItem> itensFornecedor = itens.stream()
                .filter(i -> i.getItemFornecedor().getFornecedor().getId().equals(fornecedorId))
                .toList();

        if (itensFornecedor.isEmpty()) {
            throw new RuntimeException("Nenhum item encontrado para este fornecedor");
        }

        // Remove todos os itens desse fornecedor
        for (PedidoCompraItem item : itensFornecedor) {
            pedidoCompraItemRepository.delete(item);
        }
    }
}