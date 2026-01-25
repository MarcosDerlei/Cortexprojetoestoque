package com.empresa.estoque.dashboardCategorias.service;

import com.empresa.estoque.dashboardCategorias.dto.CategoriaDashboardDTO;
import com.empresa.estoque.dashboardCategorias.dto.DashboardCategoriasResponseDTO;
import com.empresa.estoque.dashboardCategorias.dto.DashboardResumoDTO;
import com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaBigDecimalDTO;
import com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaDoubleDTO;
import com.empresa.estoque.dashboardCategorias.dto.projection.CategoriaLongCountDTO;
import com.empresa.estoque.dashboardCategorias.enums.GiroEstoque;
import com.empresa.estoque.dashboardCategorias.enums.StatusCategoria;
import com.empresa.estoque.model.CategoriaItem;
import com.empresa.estoque.model.TipoMovimento;
import com.empresa.estoque.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardCategoriaService {

    private final SubcategoriaItemRepository subcategoriaItemRepository;
    private final CategoriaItemRepository categoriaItemRepository;
    private final ItemRepository itemRepository;
    private final MovimentoEstoqueRepository movimentoRepository;
    private final EstoqueRepository estoqueRepository;

    public DashboardCategoriasResponseDTO carregarDashboard() {

        LocalDateTime inicio30d = LocalDateTime.now().minusDays(30);

        List<CategoriaItem> categorias = categoriaItemRepository.findAll();
        if (categorias.isEmpty()) {
            return new DashboardCategoriasResponseDTO(
                    new DashboardResumoDTO(0, 0, BigDecimal.ZERO, "Sem consumo"),
                    List.of()
            );
        }

        // ======================
        // ✅ BATCH QUERIES
        // ======================

        Map<Long, Long> totalItensMap = itemRepository.contarItensPorCategoria().stream()
                .collect(Collectors.toMap(CategoriaLongCountDTO::categoriaId, CategoriaLongCountDTO::total));

        Map<Long, BigDecimal> valorCategoriaMap = estoqueRepository.valorEstoquePorCategoriaBatch().stream()
                .collect(Collectors.toMap(CategoriaBigDecimalDTO::categoriaId, CategoriaBigDecimalDTO::total));

        Map<Long, Long> abaixoMinimoMap = estoqueRepository.abaixoMinimoPorCategoriaBatch().stream()
                .collect(Collectors.toMap(CategoriaLongCountDTO::categoriaId, CategoriaLongCountDTO::total));

        Map<Long, Double> saldoCategoriaMap = estoqueRepository.somarSaldoPorCategoriaBatch().stream()
                .collect(Collectors.toMap(CategoriaDoubleDTO::categoriaId, CategoriaDoubleDTO::total));

        Map<Long, Double> consumoCategoriaMap = movimentoRepository.somarSaidasPorCategoriaNoPeriodoBatch(inicio30d).stream()
                .collect(Collectors.toMap(CategoriaDoubleDTO::categoriaId, CategoriaDoubleDTO::total));

        Set<Long> categoriasCriticas = new HashSet<>(subcategoriaItemRepository.buscarCategoriasCriticasIds());
        Set<Long> categoriasAtencao = new HashSet<>(subcategoriaItemRepository.buscarCategoriasAtencaoIds());

        // ======================
        // ✅ RESUMO
        // ======================

        Integer totalCategorias = categorias.size();
        Integer categoriasCriticasCount = categoriasCriticas.size();

        BigDecimal valorTotalEstoque = estoqueRepository.calcularValorTotalEstoque();
        if (valorTotalEstoque == null) valorTotalEstoque = BigDecimal.ZERO;
        valorTotalEstoque = valorTotalEstoque.setScale(2, RoundingMode.HALF_UP);

        List<String> rankingConsumo = movimentoRepository.categoriaMaiorConsumo(
                TipoMovimento.SAIDA,
                inicio30d
        );

        String maiorConsumo30d = rankingConsumo.isEmpty() ? "Sem consumo" : rankingConsumo.get(0);

        DashboardResumoDTO resumoDTO = new DashboardResumoDTO(
                totalCategorias,
                categoriasCriticasCount,
                valorTotalEstoque,
                maiorConsumo30d
        );

        // ======================
        // ✅ CARDS
        // ======================

        List<CategoriaDashboardDTO> cards = categorias.stream().map(categoria -> {

            Long categoriaId = categoria.getId();

            Integer totalItens = totalItensMap.getOrDefault(categoriaId, 0L).intValue();

            BigDecimal valorCategoria = valorCategoriaMap.getOrDefault(categoriaId, BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);

            Integer abaixoMinimo = abaixoMinimoMap.getOrDefault(categoriaId, 0L).intValue();

            StatusCategoria status;
            if (categoriasCriticas.contains(categoriaId)) status = StatusCategoria.CRITICO;
            else if (categoriasAtencao.contains(categoriaId)) status = StatusCategoria.ATENCAO;
            else status = StatusCategoria.NORMAL;

            Double consumo30d = consumoCategoriaMap.getOrDefault(categoriaId, 0.0);
            Double saldoAtual = saldoCategoriaMap.getOrDefault(categoriaId, 0.0);

            GiroEstoque giro;
            if (consumo30d >= 100) giro = GiroEstoque.ALTO;
            else if (consumo30d >= 30) giro = GiroEstoque.MEDIO;
            else giro = GiroEstoque.BAIXO;

            BigDecimal variacao30d = BigDecimal.ZERO;
            if (saldoAtual > 0 && consumo30d > 0) {
                variacao30d = BigDecimal.valueOf((consumo30d / saldoAtual) * 100.0)
                        .setScale(0, RoundingMode.HALF_UP);
            }

            return new CategoriaDashboardDTO(
                    categoriaId,
                    categoria.getNome(),
                    totalItens,
                    valorCategoria,
                    abaixoMinimo,
                    status,
                    giro,
                    variacao30d
            );

        }).toList();

        return new DashboardCategoriasResponseDTO(resumoDTO, cards);
    }
}
