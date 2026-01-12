package com.empresa.estoque.repository;

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
        SELECT COALESCE(SUM(e.saldo), 0)
        FROM Estoque e
        WHERE e.item.categoria.id = :categoriaId
    """)
    Double somarSaldoPorCategoria(@Param("categoriaId") Long categoriaId);

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
        SELECT COALESCE(SUM(e.saldo * i.custoUnitario), 0)
        FROM Estoque e
        JOIN e.item i
        WHERE i.categoria.id = :categoriaId
          AND e.saldo > 0
    """)
    BigDecimal calcularValorEstoquePorCategoria(@Param("categoriaId") Long categoriaId);

    /**
     * Valor do estoque por subcategoria (R$)
     */
    @Query("""
        SELECT COALESCE(SUM(e.saldo * i.custoUnitario), 0)
        FROM Estoque e
        JOIN e.item i
        WHERE i.subcategoria.id = :subcategoriaId
          AND e.saldo > 0
    """)
    BigDecimal calcularValorEstoquePorSubcategoria(@Param("subcategoriaId") Long subcategoriaId);

    // ✅ ABAIXO DO MÍNIMO (ATENÇÃO):
    // saldo > 0 e menor que o mínimo do item
    @Query("""
        SELECT COUNT(e.id)
        FROM Estoque e
        WHERE e.item.categoria.id = :categoriaId
          AND e.saldo > 0
          AND e.saldo < e.item.estoqueMinimo
    """)
    Integer contarItensAbaixoMinimoPorCategoria(@Param("categoriaId") Long categoriaId);

    @Query("""
        SELECT COUNT(e.id)
        FROM Estoque e
        WHERE e.item.subcategoria.id = :subcategoriaId
          AND e.saldo > 0
          AND e.saldo < e.item.estoqueMinimo
    """)
    Integer contarItensAbaixoMinimoPorSubcategoria(@Param("subcategoriaId") Long subcategoriaId);

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
        SELECT COUNT(e.id)
        FROM Estoque e
        WHERE e.item.subcategoria.id = :subcategoriaId
          AND e.saldo <= 0
    """)
    Integer contarItensCriticosPorSubcategoria(@Param("subcategoriaId") Long subcategoriaId);

}
