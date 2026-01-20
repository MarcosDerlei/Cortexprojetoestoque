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

        PedidoCompra carrinho = obterOuCriarCarrinho(user); // ✅ pode inserir se não existir

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
                            subtotal
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

        // ✅ padroniza quantidade
        BigDecimal quantidade = dto.quantidade()
                .setScale(2, RoundingMode.HALF_UP);

        // ✅ padroniza preço no momento
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

            // ✅ garante que o preço salvo também está padronizado
            existente.setPrecoNoMomento(preco);

            pedidoCompraItemRepository.save(existente);
            return;
        }

        PedidoCompraItem novo = PedidoCompraItem.builder()
                .pedidoCompra(carrinho)
                .itemFornecedor(itemFornecedor)
                .quantidade(quantidade)
                .precoNoMomento(preco)
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
}
