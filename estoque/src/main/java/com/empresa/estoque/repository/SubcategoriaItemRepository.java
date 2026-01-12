package com.empresa.estoque.repository;

import com.empresa.estoque.model.SubcategoriaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubcategoriaItemRepository extends JpaRepository<SubcategoriaItem, Long> {
    List<SubcategoriaItem> findByCategoriaId(Long categoriaId);

    @Query("""
    SELECT s.id
    FROM SubcategoriaItem s
    JOIN Item i ON i.subcategoria.id = s.id
    JOIN Estoque e ON e.item.id = i.id
    WHERE s.categoria.id = :categoriaId
    GROUP BY s.id
    HAVING COALESCE(SUM(e.saldo), 0) <= 0
""")
    List<Long> buscarSubcategoriasCriticasIdsPorCategoria(@Param("categoriaId") Long categoriaId);

    @Query("""
        SELECT DISTINCT s.id
        FROM SubcategoriaItem s
        JOIN Item i ON i.subcategoria.id = s.id
        JOIN Estoque e ON e.item.id = i.id
        WHERE s.categoria.id = :categoriaId
          AND COALESCE(e.saldo, 0) > 0
          AND COALESCE(e.saldo, 0) <= COALESCE(i.estoqueMinimo, 0)
    """)
    List<Long> buscarSubcategoriasAtencaoIdsPorCategoria(@Param("categoriaId") Long categoriaId);
}





