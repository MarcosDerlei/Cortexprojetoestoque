package com.empresa.estoque.repository;

import com.empresa.estoque.model.Estoque;
import com.empresa.estoque.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByItem(Item item);

    Optional<Estoque> findByItemSkuAndUnidade(String sku, String unidade);

    boolean existsByItemSkuAndUnidade(String sku, String unidade);

    List<Estoque> findByUnidade(String unidade);

    List<Estoque> findByItemSkuContainingIgnoreCase(String sku);
}
