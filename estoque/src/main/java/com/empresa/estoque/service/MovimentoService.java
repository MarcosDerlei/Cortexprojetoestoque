package com.empresa.estoque.service;

import com.empresa.estoque.dto.MovimentoRequestDTO;
import com.empresa.estoque.dto.MovimentoResponseDTO;
import com.empresa.estoque.model.Estoque;
import com.empresa.estoque.model.Item;
import com.empresa.estoque.model.MovimentoEstoque;
import com.empresa.estoque.model.TipoMovimento;
import com.empresa.estoque.repository.EstoqueRepository;
import com.empresa.estoque.repository.ItemRepository;
import com.empresa.estoque.repository.MovimentoEstoqueRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class MovimentoService {

    private final MovimentoEstoqueRepository movimentoRepo;
    private final ItemRepository itemRepo;
    private final EstoqueRepository estoqueRepo;

    public MovimentoService(MovimentoEstoqueRepository movimentoRepo, ItemRepository itemRepo, EstoqueRepository estoqueRepo) {
        this.movimentoRepo = movimentoRepo;
        this.itemRepo = itemRepo;
        this.estoqueRepo = estoqueRepo;
    }

    @Transactional
    public MovimentoResponseDTO registrar(MovimentoRequestDTO dto) {
        Item item = itemRepo.findBySku(dto.sku())
                .orElseThrow(() -> new IllegalArgumentException("SKU não encontrado: " + dto.sku()));

        Estoque estoque = estoqueRepo.findByItem(item)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado para o item: " + dto.sku()));

        TipoMovimento tipo = TipoMovimento.valueOf(dto.tipo().toUpperCase());
        double quantidade = dto.quantidade();

        switch (tipo) {
            case ENTRADA, DEVOLUCAO -> estoque.setSaldo(estoque.getSaldo() + quantidade);
            case SAIDA -> {
                if (estoque.getSaldo() < quantidade)
                    throw new IllegalArgumentException("Saldo insuficiente para saída.");
                estoque.setSaldo(estoque.getSaldo() - quantidade);
            }
            case AJUSTE -> estoque.setSaldo(quantidade);
            case RESERVADO -> {
                if (estoque.getSaldo() < quantidade)
                    throw new IllegalArgumentException("Saldo insuficiente para reserva.");
                estoque.setSaldo(estoque.getSaldo() - quantidade);
                estoque.setReservado(estoque.getReservado() + quantidade);
            }
            case LIBERACAO -> {
                if (estoque.getReservado() < quantidade)
                    throw new IllegalArgumentException("Quantidade reservada insuficiente para liberação.");
                estoque.setReservado(estoque.getReservado() - quantidade);
                estoque.setSaldo(estoque.getSaldo() + quantidade);
            }
        }

        estoque.setUnidade(item.getUnidade());
        estoqueRepo.save(estoque);

        MovimentoEstoque movimento = MovimentoEstoque.builder()
                .item(item)
                .tipo(tipo)
                .quantidade(quantidade)
                .dataMovimento(LocalDateTime.now())
                .observacao(dto.observacao())
                .build();

        movimento = movimentoRepo.save(movimento);

        return toResponse(movimento);
    }

    public List<MovimentoResponseDTO> listarTodos() {
        return movimentoRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MovimentoResponseDTO> listarPorItem(String sku) {

        Item item = itemRepo.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + sku));

        return movimentoRepo.findByItemOrderByDataMovimentoDesc(item)
                .stream()
                .map(m -> new MovimentoResponseDTO(
                        m.getId(),
                        m.getItem().getSku(),
                        m.getTipo().name(),
                        m.getQuantidade(),
                        m.getDataMovimento(),
                        m.getObservacao()
                ))
                .toList();
    }


    public MovimentoResponseDTO buscarPorId(Long id) {
        MovimentoEstoque movimento = movimentoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimento não encontrado: " + id));
        return toResponse(movimento);
    }

    @Transactional
    public MovimentoResponseDTO atualizar(Long id, MovimentoRequestDTO dto) {
        MovimentoEstoque movimento = movimentoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimento não encontrado: " + id));

        // Atualiza apenas campos permitidos (ex: observação e tipo)
        movimento.setTipo(TipoMovimento.valueOf(dto.tipo().toUpperCase()));
        movimento.setQuantidade(dto.quantidade());
        movimento.setObservacao(dto.observacao());
        movimento.setDataMovimento(LocalDateTime.now());

        movimentoRepo.save(movimento);

        return toResponse(movimento);
    }

    @Transactional
    public void deletar(Long id) {
        if (!movimentoRepo.existsById(id)) {
            throw new IllegalArgumentException("Movimento não encontrado: " + id);
        }
        movimentoRepo.deleteById(id);
    }

    private MovimentoResponseDTO toResponse(MovimentoEstoque movimento) {
        return new MovimentoResponseDTO(
                movimento.getId(),
                movimento.getItem().getSku(),
                movimento.getTipo().name(),
                movimento.getQuantidade(),
                movimento.getDataMovimento(),
                movimento.getObservacao()
        );
    }
}
