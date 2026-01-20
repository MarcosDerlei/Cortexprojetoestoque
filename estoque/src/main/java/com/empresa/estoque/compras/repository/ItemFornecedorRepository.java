package com.empresa.estoque.compras.repository;

import com.empresa.estoque.compras.model.ItemFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemFornecedorRepository extends JpaRepository<ItemFornecedor, Long> {

    boolean existsByItemIdAndFornecedorId(Long itemId, Long fornecedorId);

    List<ItemFornecedor> findByItemIdOrderByIdDesc(Long itemId);

    List<ItemFornecedor> findByItemIdAndAtivoTrueOrderByIdDesc(Long itemId);
}
