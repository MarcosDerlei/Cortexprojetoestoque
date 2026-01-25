package com.empresa.estoque.repository;

import com.empresa.estoque.model.SubcategoriaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubcategoriaItemRepository extends JpaRepository<SubcategoriaItem, Long> {
    List<SubcategoriaItem> findByCategoriaId(Long categoriaId);

    @Query("""
    SELECT DISTINCT s.categoria.id
    FROM SubcategoriaItem s
    WHERE s.id IN (
        SELECT s2.id
        FROM SubcategoriaItem s2
        JOIN Item i ON i.subcategoria.id = s2.id
        JOIN Estoque e ON e.item.id = i.id
        GROUP BY s2.id
        HAVING COALESCE(SUM(e.saldo), 0) <= 0
    )
""")
    List<Long> buscarCategoriasCriticasIds();

    @Query("""
    SELECT DISTINCT s.categoria.id
    FROM SubcategoriaItem s
    WHERE s.id IN (
        SELECT DISTINCT s2.id
        FROM SubcategoriaItem s2
        JOIN Item i ON i.subcategoria.id = s2.id
        JOIN Estoque e ON e.item.id = i.id
        WHERE COALESCE(e.saldo, 0) > 0
          AND COALESCE(e.saldo, 0) <= COALESCE(i.estoqueMinimo, 0)
    )
""")
    List<Long> buscarCategoriasAtencaoIds();
}





