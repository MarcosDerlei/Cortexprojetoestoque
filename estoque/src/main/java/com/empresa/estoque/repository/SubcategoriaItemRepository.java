package com.empresa.estoque.repository;

import com.empresa.estoque.model.SubcategoriaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubcategoriaItemRepository extends JpaRepository<SubcategoriaItem, Long> {
    List<SubcategoriaItem> findByCategoriaId(Long categoriaId);
}

