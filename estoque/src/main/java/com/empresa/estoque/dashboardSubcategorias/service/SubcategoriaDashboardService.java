package com.empresa.estoque.dashboardSubcategorias.service;

import com.empresa.estoque.dashboardCategorias.enums.GiroEstoque;
import com.empresa.estoque.dashboardCategorias.enums.StatusCategoria;
import com.empresa.estoque.dashboardCategorias.service.CategoriaAnalyticsService;
import com.empresa.estoque.dashboardSubcategorias.dto.SubcategoriaDashboardDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.SubcategoriaDashboardResponseDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.SubcategoriaDashboardResumoDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaBigDecimalDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaLongCountDTO;
import com.empresa.estoque.model.MovimentoEstoque;
import com.empresa.estoque.model.SubcategoriaItem;
import com.empresa.estoque.model.TipoMovimento;
import com.empresa.estoque.repository.EstoqueRepository;
import com.empresa.estoque.repository.ItemRepository;
import com.empresa.estoque.repository.MovimentoEstoqueRepository;
import com.empresa.estoque.repository.SubcategoriaItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubcategoriaDashboardService {

    private final SubcategoriaItemRepository subcategoriaRepository;
    private final ItemRepository itemRepository;
    private final MovimentoEstoqueRepository movimentoRepository;
    private final EstoqueRepository estoqueRepository;
    private final CategoriaAnalyticsService analyticsService;

    public List<SubcategoriaDashboardDTO> listarSubcategorias(Long categoriaId) {

        // ✅ Batch queries (uma vez só)
        Map<Long, BigDecimal> valorPorSubcategoria = estoqueRepository.valorEstoquePorSubcategoriaBatch(categoriaId)
                .stream()
                .collect(Collectors.toMap(
                        SubcategoriaBigDecimalDTO::subcategoriaId,
                        SubcategoriaBigDecimalDTO::valor
                ));

        Map<Long, Integer> criticosPorSubcategoria = estoqueRepository.criticosPorSubcategoriaBatch(categoriaId)
                .stream()
                .collect(Collectors.toMap(
                        SubcategoriaLongCountDTO::subcategoriaId,
                        dto -> dto.total().intValue()
                ));

        Map<Long, Integer> abaixoMinimoPorSubcategoria = estoqueRepository.abaixoMinimoPorSubcategoriaBatch(categoriaId)
                .stream()
                .collect(Collectors.toMap(
                        SubcategoriaLongCountDTO::subcategoriaId,
                        dto -> dto.total().intValue()
                ));

        // ✅ Subcategorias da categoria
        return subcategoriaRepository.findByCategoriaId(categoriaId).stream()
                .map(subcategoria -> montarSubcategoriaDashboard(
                        subcategoria,
                        valorPorSubcategoria,
                        criticosPorSubcategoria,
                        abaixoMinimoPorSubcategoria
                ))
                .toList();
    }

    public SubcategoriaDashboardResponseDTO getDashboard(Long categoriaId) {
        return new SubcategoriaDashboardResponseDTO(
                gerarResumo(categoriaId),
                listarSubcategorias(categoriaId)
        );
    }

    /**
     * Resumo estratégico do dashboard de subcategorias (topo)
     */
    public SubcategoriaDashboardResumoDTO gerarResumo(Long categoriaId) {

        Integer totalItens = itemRepository.countByCategoriaId(categoriaId);

        List<SubcategoriaItem> subcategorias = subcategoriaRepository.findByCategoriaId(categoriaId);

        // ✅ Batch: valor + criticos + abaixoMinimo
        Map<Long, BigDecimal> valorPorSubcategoria = estoqueRepository.valorEstoquePorSubcategoriaBatch(categoriaId)
                .stream()
                .collect(Collectors.toMap(
                        SubcategoriaBigDecimalDTO::subcategoriaId,
                        SubcategoriaBigDecimalDTO::valor
                ));

        Map<Long, Integer> criticosPorSubcategoria = estoqueRepository.criticosPorSubcategoriaBatch(categoriaId)
                .stream()
                .collect(Collectors.toMap(
                        SubcategoriaLongCountDTO::subcategoriaId,
                        dto -> dto.total().intValue()
                ));

        Map<Long, Integer> abaixoMinimoPorSubcategoria = estoqueRepository.abaixoMinimoPorSubcategoriaBatch(categoriaId)
                .stream()
                .collect(Collectors.toMap(
                        SubcategoriaLongCountDTO::subcategoriaId,
                        dto -> dto.total().intValue()
                ));

        // ✅ 1) Subcategorias críticas
        Integer subcategoriasCriticas = (int) subcategorias.stream()
                .filter(s -> criticosPorSubcategoria.getOrDefault(s.getId(), 0) > 0)
                .count();

        // ✅ 2) Subcategorias abaixo mínimo (atenção) -> abaixoMinimo > 0 e criticos == 0
        Integer subcategoriasAbaixoMinimo = (int) subcategorias.stream()
                .filter(s -> criticosPorSubcategoria.getOrDefault(s.getId(), 0) == 0)
                .filter(s -> abaixoMinimoPorSubcategoria.getOrDefault(s.getId(), 0) > 0)
                .count();

        // ✅ 3) Valor total estoque da categoria (somando subcategorias)
        BigDecimal valorTotalEstoque = valorPorSubcategoria.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SubcategoriaDashboardResumoDTO(
                totalItens,
                subcategoriasCriticas,
                valorTotalEstoque,
                subcategoriasAbaixoMinimo
        );
    }

    private SubcategoriaDashboardDTO montarSubcategoriaDashboard(
            SubcategoriaItem subcategoria,
            Map<Long, BigDecimal> valorPorSubcategoria,
            Map<Long, Integer> criticosPorSubcategoria,
            Map<Long, Integer> abaixoMinimoPorSubcategoria
    ) {

        Long subcategoriaId = subcategoria.getId();

        // ⚡ ainda pode otimizar depois com batch (mas ok por enquanto)
        Integer totalItens = itemRepository.findBySubcategoriaId(subcategoriaId).size();

        BigDecimal valorSubcategoria = valorPorSubcategoria.getOrDefault(subcategoriaId, BigDecimal.ZERO);

        int itensCriticos = criticosPorSubcategoria.getOrDefault(subcategoriaId, 0);
        int itensAbaixoMinimo = abaixoMinimoPorSubcategoria.getOrDefault(subcategoriaId, 0);

        StatusCategoria status;
        if (itensCriticos > 0) status = StatusCategoria.CRITICO;
        else if (itensAbaixoMinimo > 0) status = StatusCategoria.ATENCAO;
        else status = StatusCategoria.NORMAL;

        // Giro (últimos 30 dias)
        List<MovimentoEstoque> movimentos = movimentoRepository.findMovimentosPorSubcategoriaEPeriodo(
                subcategoriaId,
                TipoMovimento.SAIDA,
                LocalDateTime.now().minusDays(30)
        );

        GiroEstoque giro = analyticsService.calcularGiro(movimentos);

        return new SubcategoriaDashboardDTO(
                subcategoriaId,
                subcategoria.getNome(),
                totalItens,
                itensAbaixoMinimo,
                valorSubcategoria,
                status,
                giro
        );
    }
}
