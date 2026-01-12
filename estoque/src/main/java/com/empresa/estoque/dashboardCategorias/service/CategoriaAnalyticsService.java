package com.empresa.estoque.dashboardCategorias.service;

import com.empresa.estoque.dashboardCategorias.enums.GiroEstoque;
import com.empresa.estoque.dashboardCategorias.enums.StatusCategoria;
import com.empresa.estoque.model.MovimentoEstoque;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoriaAnalyticsService {

    public StatusCategoria calcularStatus(int itensAbaixoMinimo) {
        if (itensAbaixoMinimo > 5) return StatusCategoria.CRITICO;
        if (itensAbaixoMinimo > 0) return StatusCategoria.ATENCAO;
        return StatusCategoria.NORMAL;
    }

    public GiroEstoque calcularGiro(List<MovimentoEstoque> movimentos30d) {

        if (movimentos30d == null || movimentos30d.isEmpty()) {
            return GiroEstoque.BAIXO;
        }

        int totalSaidas = movimentos30d.stream()
                .mapToInt(m -> m.getQuantidade().intValue())
                .sum();

        if (totalSaidas >= 100) {
            return GiroEstoque.ALTO;
        }

        if (totalSaidas >= 30) {
            return GiroEstoque.MEDIO;
        }

        return GiroEstoque.BAIXO;
    }
}

