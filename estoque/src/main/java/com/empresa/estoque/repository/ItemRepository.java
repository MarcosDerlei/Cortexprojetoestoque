package com.empresa.estoque.repository;

import com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaLongCountDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaLongCountDTO;
import com.empresa.estoque.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
    SELECT new com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaLongCountDTO(
        i.categoria.id,
        COUNT(i.id)
    )
    FROM Item i
    GROUP BY i.categoria.id
""")
    List<CategoriaLongCountDTO> contarItensPorCategoria();

    // ✅ NOVO: Batch query para contar itens por subcategoria (evita N+1)
    @Query("""
    SELECT new com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaLongCountDTO(
        i.subcategoria.id,
        COUNT(i.id)
    )
    FROM Item i
    WHERE i.categoria.id = :categoriaId
    GROUP BY i.subcategoria.id
""")
    List<SubcategoriaLongCountDTO> contarItensPorSubcategoriaBatch(@Param("categoriaId") Long categoriaId);
}