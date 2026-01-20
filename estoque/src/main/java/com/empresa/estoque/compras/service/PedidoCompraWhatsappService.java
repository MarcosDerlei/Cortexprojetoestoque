package com.empresa.estoque.compras.service;

import com.empresa.estoque.compras.model.PedidoCompraItem;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PedidoCompraWhatsappService {

    public String montarWhatsappUrl(Long pedidoId, String telefone, String nomeFornecedor, String dataCriacao, List<String> linhasItens) {
        String itensTexto = String.join("\n", linhasItens);

        String mensagem = """
Olá %s! Gostaria de fazer um pedido:

Pedido #%d - %s
%s

Pode confirmar disponibilidade e prazo? Obrigado!
""".formatted(nomeFornecedor, pedidoId, dataCriacao, itensTexto);

        return "https://wa.me/" + telefone + "?text=" +
                URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
    }

    public String formatarData(java.time.LocalDateTime data) {
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
