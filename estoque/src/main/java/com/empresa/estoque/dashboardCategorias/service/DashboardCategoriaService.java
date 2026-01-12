package com.empresa.estoque.dashboardCategorias.service;

import com.empresa.estoque.dashboardCategorias.dto.CategoriaDashboardDTO;
import com.empresa.estoque.dashboardCategorias.dto.DashboardResumoDTO;
import com.empresa.estoque.dashboardCategorias.enums.GiroEstoque;
import com.empresa.estoque.dashboardCategorias.enums.StatusCategoria;
import com.empresa.estoque.model.CategoriaItem;
import com.empresa.estoque.model.MovimentoEstoque;
import com.empresa.estoque.model.TipoMovimento;
import com.empresa.estoque.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardCategoriaService {

    private final SubcategoriaItemRepository subcategoriaItemRepository;
    private final CategoriaItemRepository categoriaItemRepository;
    private final ItemRepository itemRepository;
    private final MovimentoEstoqueRepository movimentoRepository;
    private final EstoqueRepository estoqueRepository;
    private final CategoriaAnalyticsService analyticsService;

    public List<CategoriaDashboardDTO> listarCategorias() {
        return categoriaItemRepository.findAll().stream()
                .map(this::montarCategoriaDashboard)
                .toList();
    }

    public DashboardResumoDTO gerarResumo() {

        Integer totalCategorias = Math.toIntExact(categoriaItemRepository.count());

        Integer categoriasCriticas = (int) categoriaItemRepository.findAll().stream()
                .filter(categoria ->
                        calcularStatusPorSubcategorias(categoria.getId()) == StatusCategoria.CRITICO
                )
                .count();

        BigDecimal valorTotalEstoque =
                estoqueRepository.calcularValorTotalEstoque();

        if (valorTotalEstoque == null) {
            valorTotalEstoque = BigDecimal.ZERO;
        }

        valorTotalEstoque = valorTotalEstoque.setScale(2, RoundingMode.HALF_UP);

        List<String> rankingConsumo =
                movimentoRepository.categoriaMaiorConsumo(
                        TipoMovimento.SAIDA,
                        LocalDateTime.now().minusDays(30)
                );

        String maiorConsumo30d =
                rankingConsumo.isEmpty() ? "Sem consumo" : rankingConsumo.get(0);

        return new DashboardResumoDTO(
                totalCategorias,
                categoriasCriticas,
                valorTotalEstoque,
                maiorConsumo30d
        );
    }

    /**
     * Regra principal:
     * - se existir subcategoria CRITICO -> categoria CRITICO
     * - senão se existir subcategoria ATENCAO -> categoria ATENCAO
     * - senão NORMAL
     */
    private StatusCategoria calcularStatusPorSubcategorias(Long categoriaId) {

        int subcategoriasCriticas =
                subcategoriaItemRepository.buscarSubcategoriasCriticasIdsPorCategoria(categoriaId).size();

        if (subcategoriasCriticas > 0) {
            return StatusCategoria.CRITICO;
        }

        int subcategoriasAtencao =
                subcategoriaItemRepository.buscarSubcategoriasAtencaoIdsPorCategoria(categoriaId).size();

        if (subcategoriasAtencao > 0) {
            return StatusCategoria.ATENCAO;
        }

        return StatusCategoria.NORMAL;
    }

    private CategoriaDashboardDTO montarCategoriaDashboard(CategoriaItem categoria) {

        Long categoriaId = categoria.getId();
        LocalDateTime inicio30d = LocalDateTime.now().minusDays(30);

        Integer totalItens =
                itemRepository.countByCategoriaId(categoriaId);

        BigDecimal valorCategoria =
                estoqueRepository.calcularValorEstoquePorCategoria(categoriaId);

        if (valorCategoria == null) {
            valorCategoria = BigDecimal.ZERO;
        }

        valorCategoria = valorCategoria.setScale(2, RoundingMode.HALF_UP);

        Integer abaixoMinimo =
                estoqueRepository.contarItensAbaixoMinimoPorCategoria(categoriaId);

        // ✅ status agora vem das subcategorias
        StatusCategoria status =
                calcularStatusPorSubcategorias(categoriaId);

        List<MovimentoEstoque> movimentos =
                movimentoRepository.findMovimentosPorCategoriaEPeriodo(
                        categoriaId,
                        TipoMovimento.SAIDA,
                        inicio30d
                );

        GiroEstoque giro =
                analyticsService.calcularGiro(movimentos);

        Double consumo30d =
                movimentoRepository.somarSaidasPorCategoriaNoPeriodo(
                        categoriaId,
                        inicio30d
                );

        Double saldoAtual =
                estoqueRepository.somarSaldoPorCategoria(categoriaId);

        BigDecimal variacao30d = BigDecimal.ZERO;

        if (saldoAtual != null && saldoAtual > 0 && consumo30d != null && consumo30d > 0) {
            variacao30d = BigDecimal.valueOf((consumo30d / saldoAtual) * 100.0)
                    .setScale(0, RoundingMode.HALF_UP);
        }

        return new CategoriaDashboardDTO(
                categoria.getId(),
                categoria.getNome(),
                totalItens,
                valorCategoria,
                abaixoMinimo != null ? abaixoMinimo : 0,
                status,
                giro,
                variacao30d
        );
    }
}
