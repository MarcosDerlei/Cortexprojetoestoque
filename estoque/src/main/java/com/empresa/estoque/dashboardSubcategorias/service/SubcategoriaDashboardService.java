package com.empresa.estoque.dashboardSubcategorias.service;

import com.empresa.estoque.dashboardCategorias.enums.GiroEstoque;
import com.empresa.estoque.dashboardCategorias.enums.StatusCategoria;
import com.empresa.estoque.dashboardSubcategorias.dto.SubcategoriaDashboardDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.SubcategoriaDashboardResponseDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.SubcategoriaDashboardResumoDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaBigDecimalDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.projection.SubcategoriaLongCountDTO;
import com.empresa.estoque.model.SubcategoriaItem;
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

    /**
     * ✅ OTIMIZADO: Um único método que faz TODAS as queries batch UMA VEZ
     * e monta tanto o resumo quanto os cards
     */
    public SubcategoriaDashboardResponseDTO getDashboard(Long categoriaId) {

        // ======================
        // ✅ BATCH QUERIES (UMA VEZ SÓ!)
        // ======================

        List<SubcategoriaItem> subcategorias = subcategoriaRepository.findByCategoriaId(categoriaId);

        if (subcategorias.isEmpty()) {
            return new SubcategoriaDashboardResponseDTO(
                    new SubcategoriaDashboardResumoDTO(0, 0, BigDecimal.ZERO, 0),
                    List.of()
            );
        }

        // 1. Valor por subcategoria
        Map<Long, BigDecimal> valorPorSubcategoria = estoqueRepository
                .valorEstoquePorSubcategoriaBatch(categoriaId)
                .stream()
                .collect(Collectors.toMap(
                        SubcategoriaBigDecimalDTO::subcategoriaId,
                        SubcategoriaBigDecimalDTO::valor,
                        (a, b) -> a // em caso de duplicata, mantém o primeiro
                ));

        // 2. Críticos por subcategoria (saldo <= 0)
        Map<Long, Integer> criticosPorSubcategoria = estoqueRepository
                .criticosPorSubcategoriaBatch(categoriaId)
                .stream()
                .collect(Collectors.toMap(
                        SubcategoriaLongCountDTO::subcategoriaId,
                        dto -> dto.total().intValue(),
                        (a, b) -> a
                ));

        // 3. Abaixo do mínimo por subcategoria (saldo > 0 mas < mínimo)
        Map<Long, Integer> abaixoMinimoPorSubcategoria = estoqueRepository
                .abaixoMinimoPorSubcategoriaBatch(categoriaId)
                .stream()
                .collect(Collectors.toMap(
                        SubcategoriaLongCountDTO::subcategoriaId,
                        dto -> dto.total().intValue(),
                        (a, b) -> a
                ));

        // 4. ✅ NOVO: Total de itens por subcategoria (BATCH - evita N+1)
        Map<Long, Integer> itensPorSubcategoria = itemRepository
                .contarItensPorSubcategoriaBatch(categoriaId)
                .stream()
                .collect(Collectors.toMap(
                        SubcategoriaLongCountDTO::subcategoriaId,
                        dto -> dto.total().intValue(),
                        (a, b) -> a
                ));

        // 5. ✅ NOVO: Consumo por subcategoria nos últimos 30 dias (para calcular giro)
        LocalDateTime inicio30d = LocalDateTime.now().minusDays(30);
        Map<Long, Double> consumoPorSubcategoria = movimentoRepository
                .somarSaidasPorSubcategoriaNoPeriodoBatch(categoriaId, inicio30d)
                .stream()
                .collect(Collectors.toMap(
                        dto -> dto.subcategoriaId(),
                        dto -> dto.total(),
                        (a, b) -> a
                ));

        // ======================
        // ✅ MONTAR RESUMO
        // ======================

        Integer totalItens = itensPorSubcategoria.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        Integer subcategoriasCriticas = (int) subcategorias.stream()
                .filter(s -> criticosPorSubcategoria.getOrDefault(s.getId(), 0) > 0)
                .count();

        Integer subcategoriasAbaixoMinimo = (int) subcategorias.stream()
                .filter(s -> criticosPorSubcategoria.getOrDefault(s.getId(), 0) == 0)
                .filter(s -> abaixoMinimoPorSubcategoria.getOrDefault(s.getId(), 0) > 0)
                .count();

        BigDecimal valorTotalEstoque = valorPorSubcategoria.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SubcategoriaDashboardResumoDTO resumo = new SubcategoriaDashboardResumoDTO(
                totalItens,
                subcategoriasCriticas,
                valorTotalEstoque,
                subcategoriasAbaixoMinimo
        );

        // ======================
        // ✅ MONTAR CARDS
        // ======================

        List<SubcategoriaDashboardDTO> cards = subcategorias.stream()
                .map(subcategoria -> {
                    Long id = subcategoria.getId();

                    Integer qtdItens = itensPorSubcategoria.getOrDefault(id, 0);
                    BigDecimal valor = valorPorSubcategoria.getOrDefault(id, BigDecimal.ZERO);
                    int criticos = criticosPorSubcategoria.getOrDefault(id, 0);
                    int abaixoMinimo = abaixoMinimoPorSubcategoria.getOrDefault(id, 0);
                    Double consumo = consumoPorSubcategoria.getOrDefault(id, 0.0);

                    // Status
                    StatusCategoria status;
                    if (criticos > 0) status = StatusCategoria.CRITICO;
                    else if (abaixoMinimo > 0) status = StatusCategoria.ATENCAO;
                    else status = StatusCategoria.NORMAL;

                    // Giro baseado no consumo dos últimos 30 dias
                    GiroEstoque giro;
                    if (consumo >= 100) giro = GiroEstoque.ALTO;
                    else if (consumo >= 30) giro = GiroEstoque.MEDIO;
                    else giro = GiroEstoque.BAIXO;

                    return new SubcategoriaDashboardDTO(
                            id,
                            subcategoria.getNome(),
                            qtdItens,
                            abaixoMinimo,
                            valor,
                            status,
                            giro
                    );
                })
                .toList();

        return new SubcategoriaDashboardResponseDTO(resumo, cards);
    }
}