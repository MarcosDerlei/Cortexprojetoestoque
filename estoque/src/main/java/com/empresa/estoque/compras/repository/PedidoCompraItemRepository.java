package com.empresa.estoque.compras.repository;

import com.empresa.estoque.compras.model.PedidoCompraItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoCompraItemRepository extends JpaRepository<PedidoCompraItem, Long> {
    List<PedidoCompraItem> findByPedidoCompraId(Long pedidoCompraId);

    Optional<PedidoCompraItem> findByPedidoCompraIdAndItemFornecedorId(Long pedidoCompraId, Long itemFornecedorId);
}
