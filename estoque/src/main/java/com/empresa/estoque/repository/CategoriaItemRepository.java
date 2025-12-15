package com.empresa.estoque.repository;

import com.empresa.estoque.model.CategoriaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoriaItemRepository extends JpaRepository<CategoriaItem, Long> {Optional<CategoriaItem> findByCodigo(String codigo);
}
