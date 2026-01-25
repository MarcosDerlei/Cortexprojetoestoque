package com.empresa.estoque.repository;

import com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaDoubleDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaDoubleDTO;
import com.empresa.estoque.model.Item;
import com.empresa.estoque.model.MovimentoEstoque;
import com.empresa.estoque.model.TipoMovimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {

    List<MovimentoEstoque> findByItemOrderByDataMovimentoDesc(Item item);

    // Dashboard – Categoria

    @Query("""
        SELECT m
        FROM MovimentoEstoque m
        JOIN m.item i
        WHERE i.categoria.id = :categoriaId
          AND m.tipo = :tipo
          AND m.dataMovimento >= :dataInicio
    """)
    List<MovimentoEstoque> findMovimentosPorCategoriaEPeriodo(
            @Param("categoriaId") Long categoriaId,
            @Param("tipo") TipoMovimento tipo,
            @Param("dataInicio") LocalDateTime dataInicio
    );

    @Query("""
        SELECT i.categoria.nome
        FROM MovimentoEstoque m
        JOIN m.item i
        WHERE m.tipo = :tipo
          AND m.dataMovimento >= :dataInicio
        GROUP BY i.categoria.nome
        ORDER BY SUM(m.quantidade) DESC
    """)
    List<String> categoriaMaiorConsumo(
            @Param("tipo") TipoMovimento tipo,
            @Param("dataInicio") LocalDateTime dataInicio
    );

    // Cálculo de quantidade atual (por item)

    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN m.tipo = 'ENTRADA' THEN m.quantidade
                WHEN m.tipo = 'SAIDA' THEN -m.quantidade
                ELSE 0
            END
        ), 0)
        FROM MovimentoEstoque m
        WHERE m.item.id = :itemId
    """)
    Double calcularQuantidadeAtual(@Param("itemId") Long itemId);

    // Valor total do estoque (GLOBAL)

    @Query("""
        SELECT COALESCE(SUM(
            (
                SELECT COALESCE(SUM(
                    CASE
                        WHEN m2.tipo = 'ENTRADA' THEN m2.quantidade
                        WHEN m2.tipo = 'SAIDA' THEN -m2.quantidade
                        ELSE 0
                    END
                ), 0)
                FROM MovimentoEstoque m2
                WHERE m2.item = i
            ) * i.custoUnitario
        ), 0)
        FROM Item i
    """)
    Double calcularValorTotalEstoque();

    // ========================
    // Valor do estoque por categoria
    // ========================
    @Query("""
        SELECT COALESCE(SUM(
            (
                SELECT COALESCE(SUM(
                    CASE
                        WHEN me.tipo = 'ENTRADA' THEN me.quantidade
                        WHEN me.tipo = 'SAIDA' THEN -me.quantidade
                        ELSE 0
                    END
                ), 0)
                FROM MovimentoEstoque me
                WHERE me.item = i
            ) * i.custoUnitario
        ), 0)
        FROM Item i
        WHERE i.categoria.id = :categoriaId
    """)
    Double calcularValorEstoquePorCategoria(@Param("categoriaId") Long categoriaId);

    // ========================
    // Dashboard – Subcategoria
    // ========================

    @Query("""
        SELECT COUNT(DISTINCT i.id)
        FROM Item i
        WHERE i.subcategoria.id = :subcategoriaId
          AND (
              SELECT COALESCE(SUM(
                  CASE
                      WHEN m.tipo = 'ENTRADA' THEN m.quantidade
                      WHEN m.tipo = 'SAIDA' THEN -m.quantidade
                      ELSE 0
                  END
              ), 0)
              FROM MovimentoEstoque m
              WHERE m.item = i
          ) < i.estoqueMinimo
    """)
    Integer contarItensAbaixoMinimoPorSubcategoria(
            @Param("subcategoriaId") Long subcategoriaId
    );

    /**
     * ✅ CORRIGIDO
     * Calcula o valor do estoque por subcategoria
     * somando o saldo consolidado de cada item
     */
    @Query("""
        SELECT COALESCE(SUM(
            (
                SELECT COALESCE(SUM(
                    CASE
                        WHEN me.tipo = 'ENTRADA' THEN me.quantidade
                        WHEN me.tipo = 'SAIDA' THEN -me.quantidade
                        ELSE 0
                    END
                ), 0)
                FROM MovimentoEstoque me
                WHERE me.item = i
            ) * i.custoUnitario
        ), 0)
        FROM Item i
        WHERE i.subcategoria.id = :subcategoriaId
    """)
    Double calcularValorEstoquePorSubcategoria(
            @Param("subcategoriaId") Long subcategoriaId
    );

    // ========================
    // Movimentos por subcategoria (histórico)
    // ========================
    @Query("""
        SELECT m
        FROM MovimentoEstoque m
        WHERE m.item.subcategoria.id = :subcategoriaId
          AND m.tipo = :tipo
          AND m.dataMovimento >= :inicio
    """)
    List<MovimentoEstoque> findMovimentosPorSubcategoriaEPeriodo(
            @Param("subcategoriaId") Long subcategoriaId,
            @Param("tipo") TipoMovimento tipo,
            @Param("inicio") LocalDateTime inicio
    );

    // ========================
    // BATCH QUERIES (OTIMIZAÇÃO)
    // ========================

    @Query("""
    SELECT new com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaDoubleDTO(
        m.item.categoria.id,
        COALESCE(SUM(m.quantidade), 0)
    )
    FROM MovimentoEstoque m
    WHERE m.tipo = 'SAIDA'
      AND m.dataMovimento >= :inicio
    GROUP BY m.item.categoria.id
""")
    List<CategoriaDoubleDTO> somarSaidasPorCategoriaNoPeriodoBatch(@Param("inicio") LocalDateTime inicio);

    // ✅ NOVO: Batch query para somar saídas por SUBCATEGORIA (evita N+1)
    @Query("""
    SELECT new com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaDoubleDTO(
        m.item.subcategoria.id,
        COALESCE(SUM(m.quantidade), 0.0)
    )
    FROM MovimentoEstoque m
    WHERE m.item.categoria.id = :categoriaId
      AND m.tipo = 'SAIDA'
      AND m.dataMovimento >= :inicio
    GROUP BY m.item.subcategoria.id
""")
    List<SubcategoriaDoubleDTO> somarSaidasPorSubcategoriaNoPeriodoBatch(
            @Param("categoriaId") Long categoriaId,
            @Param("inicio") LocalDateTime inicio
    );
}