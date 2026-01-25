package com.empresa.estoque.repository;

import com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaBigDecimalDTO;
import com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaDoubleDTO;
import com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaLongCountDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaBigDecimalDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaLongCountDTO;
import com.empresa.estoque.model.Estoque;
import com.empresa.estoque.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByItem(Item item);

    Optional<Estoque> findByItemSkuAndUnidade(String sku, String unidade);

    boolean existsByItemSkuAndUnidade(String sku, String unidade);

    List<Estoque> findByUnidade(String unidade);

    List<Estoque> findByItemSkuContainingIgnoreCase(String sku);

    /**
     * Soma do saldo total da categoria (quantidade)
     */
    @Query("""
    SELECT new com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaDoubleDTO(
        e.item.categoria.id,
        COALESCE(SUM(e.saldo), 0)
    )
    FROM Estoque e
    GROUP BY e.item.categoria.id
""")
    List<CategoriaDoubleDTO> somarSaldoPorCategoriaBatch();


    /**
     * Valor total do estoque (R$)
     */
    @Query("""
        SELECT COALESCE(SUM(e.saldo * i.custoUnitario), 0)
        FROM Estoque e
        JOIN e.item i
        WHERE e.saldo > 0
    """)
    BigDecimal calcularValorTotalEstoque();

    /**
     * Valor do estoque por categoria (R$)
     */
    @Query("""
    SELECT new com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaBigDecimalDTO(
        i.categoria.id,
        COALESCE(
            SUM(
                CAST(e.saldo AS bigdecimal) * CAST(i.custoUnitario AS bigdecimal)
            ),
            0
        )
    )
    FROM Estoque e
    JOIN e.item i
    WHERE e.saldo > 0
    GROUP BY i.categoria.id
""")
    List<CategoriaBigDecimalDTO> valorEstoquePorCategoriaBatch();



    /**
     * Valor do estoque por subcategoria (R$)
     */
    @Query(value = """
    SELECT
        i.subcategoria_id AS subcategoriaId,
        COALESCE(
            SUM(
                CAST(e.saldo AS numeric) * CAST(i.custo_unitario AS numeric)
            ),
            0
        ) AS valor
    FROM estoques e
    JOIN itens i ON i.id = e.item_id
    WHERE i.categoria_id = :categoriaId
      AND e.saldo > 0
    GROUP BY i.subcategoria_id
""", nativeQuery = true)
    List<SubcategoriaBigDecimalDTO> valorEstoquePorSubcategoriaBatch(
            @Param("categoriaId") Long categoriaId
    );

    // ✅ ABAIXO DO MÍNIMO (ATENÇÃO):
    // saldo > 0 e menor que o mínimo do item

    @Query("""
    SELECT new com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaLongCountDTO(
        e.item.categoria.id,
        COUNT(e.id)
    )
    FROM Estoque e
    WHERE e.saldo > 0
      AND e.saldo < e.item.estoqueMinimo
    GROUP BY e.item.categoria.id
""")
    List<CategoriaLongCountDTO> abaixoMinimoPorCategoriaBatch();

    @Query("""
    SELECT new com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaLongCountDTO(
        e.item.subcategoria.id,
        COUNT(e.id)
    )
    FROM Estoque e
    WHERE e.item.categoria.id = :categoriaId
      AND e.saldo > 0
      AND e.saldo < e.item.estoqueMinimo
    GROUP BY e.item.subcategoria.id
""")
    List<SubcategoriaLongCountDTO> abaixoMinimoPorSubcategoriaBatch(@Param("categoriaId") Long categoriaId);

    // ✅ ITENS CRÍTICOS:
    // saldo <= 0
    @Query("""
        SELECT COUNT(e.id)
        FROM Estoque e
        WHERE e.item.categoria.id = :categoriaId
          AND e.saldo <= 0
    """)
    Integer contarItensCriticosPorCategoria(@Param("categoriaId") Long categoriaId);

    @Query("""
    SELECT new com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaLongCountDTO(
        e.item.subcategoria.id,
        COUNT(e.id)
    )
    FROM Estoque e
    WHERE e.item.categoria.id = :categoriaId
      AND e.saldo <= 0
    GROUP BY e.item.subcategoria.id
""")
    List<SubcategoriaLongCountDTO> criticosPorSubcategoriaBatch(@Param("categoriaId") Long categoriaId);

}
