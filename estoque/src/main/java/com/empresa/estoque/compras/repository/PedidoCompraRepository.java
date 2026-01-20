package com.empresa.estoque.compras.repository;

import com.empresa.estoque.compras.model.PedidoCompra;
import com.empresa.estoque.compras.model.StatusPedidoCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {
    Optional<PedidoCompra> findByUserIdAndStatus(Long userId, StatusPedidoCompra status);
}
