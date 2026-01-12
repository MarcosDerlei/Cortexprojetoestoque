package com.empresa.estoque.dashboardSubcategorias.service;

import com.empresa.estoque.dashboardSubcategorias.dto.SubcategoriaDashboardDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.SubcategoriaDashboardResponseDTO;
import com.empresa.estoque.dashboardSubcategorias.dto.SubcategoriaDashboardResumoDTO;
import com.empresa.estoque.dashboardCategorias.enums.GiroEstoque;
import com.empresa.estoque.dashboardCategorias.enums.StatusCategoria;
import com.empresa.estoque.model.MovimentoEstoque;
import com.empresa.estoque.model.SubcategoriaItem;
import com.empresa.estoque.model.TipoMovimento;
import com.empresa.estoque.repository.EstoqueRepository;
import com.empresa.estoque.repository.ItemRepository;
import com.empresa.estoque.repository.MovimentoEstoqueRepository;
import com.empresa.estoque.repository.SubcategoriaItemRepository;
import com.empresa.estoque.dashboardCategorias.service.CategoriaAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubcategoriaDashboardService {

    private final SubcategoriaItemRepository subcategoriaRepository;
    private final ItemRepository itemRepository;
    private final MovimentoEstoqueRepository movimentoRepository;
    private final EstoqueRepository estoqueRepository;
    private final CategoriaAnalyticsService analyticsService;

    public List<SubcategoriaDashboardDTO> listarSubcategorias(Long categoriaId) {

        return subcategoriaRepository.findByCategoriaId(categoriaId).stream()
                .map(this::montarSubcategoriaDashboard)
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

        // 🔹 Total de itens das subcategorias da categoria
        Integer totalItens =
                itemRepository.countByCategoriaId(categoriaId);

        List<SubcategoriaItem> subcategorias =
                subcategoriaRepository.findByCategoriaId(categoriaId);

        // 🔹 Subcategorias críticas (tem pelo menos 1 item com saldo <= 0)
        Integer subcategoriasCriticas = (int)
                subcategorias.stream()
                        .filter(subcategoria -> {
                            Integer criticos =
                                    estoqueRepository.contarItensCriticosPorSubcategoria(subcategoria.getId());

                            int criticosSeguro =
                                    criticos != null ? criticos : 0;

                            return criticosSeguro > 0;
                        })
                        .count();

        // 🔹 Valor total do estoque da categoria (R$)
        BigDecimal valorTotalEstoque =
                estoqueRepository.calcularValorEstoquePorCategoria(categoriaId);

        if (valorTotalEstoque == null) {
            valorTotalEstoque = BigDecimal.ZERO;
        }

        // 🔹 Subcategorias abaixo do mínimo (ATENÇÃO)
        // regra: tem abaixo mínimo > 0, mas NÃO tem críticos
        Integer subcategoriasAbaixoMinimo = (int)
                subcategorias.stream()
                        .filter(subcategoria -> {

                            Integer criticos =
                                    estoqueRepository.contarItensCriticosPorSubcategoria(subcategoria.getId());

                            Integer abaixoMinimo =
                                    estoqueRepository.contarItensAbaixoMinimoPorSubcategoria(subcategoria.getId());

                            int criticosSeguro = criticos != null ? criticos : 0;
                            int abaixoMinimoSeguro = abaixoMinimo != null ? abaixoMinimo : 0;

                            return criticosSeguro == 0 && abaixoMinimoSeguro > 0;
                        })
                        .count();

        return new SubcategoriaDashboardResumoDTO(
                totalItens,
                subcategoriasCriticas,
                valorTotalEstoque,
                subcategoriasAbaixoMinimo
        );
    }

    /**
     * Monta o card individual da subcategoria
     */
    private SubcategoriaDashboardDTO montarSubcategoriaDashboard(SubcategoriaItem subcategoria) {

        Long subcategoriaId = subcategoria.getId();

        // 🔹 Total de itens cadastrados na subcategoria
        Integer totalItens =
                itemRepository.findBySubcategoriaId(subcategoriaId).size();

        // 🔹 Valor do estoque da subcategoria
        BigDecimal valorSubcategoria =
                estoqueRepository.calcularValorEstoquePorSubcategoria(subcategoriaId);

        if (valorSubcategoria == null) {
            valorSubcategoria = BigDecimal.ZERO;
        }

        // 🔹 Itens críticos e abaixo do mínimo
        Integer criticos =
                estoqueRepository.contarItensCriticosPorSubcategoria(subcategoriaId);

        Integer abaixoMinimo =
                estoqueRepository.contarItensAbaixoMinimoPorSubcategoria(subcategoriaId);

        int itensCriticos =
                criticos != null ? criticos : 0;

        int itensAbaixoMinimo =
                abaixoMinimo != null ? abaixoMinimo : 0;

        // 🔹 Status do card (prioridade: CRÍTICO > ATENÇÃO > NORMAL)
        StatusCategoria status;
        if (itensCriticos > 0) {
            status = StatusCategoria.CRITICO;
        } else if (itensAbaixoMinimo > 0) {
            status = StatusCategoria.ATENCAO;
        } else {
            status = StatusCategoria.NORMAL;
        }

        // 🔹 Giro (últimos 30 dias)
        List<MovimentoEstoque> movimentos =
                movimentoRepository.findMovimentosPorSubcategoriaEPeriodo(
                        subcategoriaId,
                        TipoMovimento.SAIDA,
                        LocalDateTime.now().minusDays(30)
                );

        GiroEstoque giro =
                analyticsService.calcularGiro(movimentos);

        return new SubcategoriaDashboardDTO(
                subcategoria.getId(),
                subcategoria.getNome(),
                totalItens,
                itensAbaixoMinimo,
                valorSubcategoria,
                status,
                giro
        );
    }
}
