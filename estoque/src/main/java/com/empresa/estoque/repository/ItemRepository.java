package com.empresa.estoque.repository;

import com.empresa.estoque.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item,Long> {

    Integer countByCategoriaId(Long categoriaId);

    Optional<Item> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Item> findByAtivoTrue();

    List<Item> findByDescricaoContainingIgnoreCaseOrSkuContainingIgnoreCase(String desc, String sku);

    List<Item> findByCategoriaId(Long categoriaId);

    List<Item> findBySubcategoriaId(Long subcategoriaId);
}



