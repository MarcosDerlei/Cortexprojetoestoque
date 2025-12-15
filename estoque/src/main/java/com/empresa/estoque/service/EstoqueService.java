package com.empresa.estoque.service;

import com.empresa.estoque.dto.EstoqueRequestDTO;
import com.empresa.estoque.dto.EstoqueResponseDTO;
import com.empresa.estoque.dto.MovimentoRequestDTO;
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

public class EstoqueService {

    private final ItemRepository itemRepo;
    private final EstoqueRepository estoqueRepo;
    private final MovimentoEstoqueRepository movRepo;

    public EstoqueService(ItemRepository itemRepo, EstoqueRepository estoqueRepo, MovimentoEstoqueRepository movRepo) {
        this.itemRepo = itemRepo;
        this.estoqueRepo = estoqueRepo;
        this.movRepo = movRepo;
    }

    @Transactional
    public void cadastrarEstoque(EstoqueRequestDTO dto) {
        if (estoqueRepo.existsByItemSkuAndUnidade(dto.sku(), dto.unidade())) {
            throw new IllegalArgumentException("Já existe um estoque para este SKU e unidade.");
        }

        Item item = itemRepo.findBySku(dto.sku())
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + dto.sku()));

        Estoque estoque = Estoque.builder()
                .item(item)
                .unidade(dto.unidade())
                .saldo(dto.saldo())
                .reservado(dto.reservado())
                .pontoReposicao(dto.pontoReposicao())
                .build();

        estoqueRepo.save(estoque);
    }

    @Transactional
    public void registrarMovimento(MovimentoRequestDTO dto) {
        Item item = itemRepo.findBySku(dto.sku())
                .orElseThrow(() -> new IllegalArgumentException("SKU não encontrado: " + dto.sku()));

        Estoque est = estoqueRepo.findByItemSkuAndUnidade(dto.sku(), item.getUnidade())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Estoque não encontrado para SKU e unidade: " + dto.sku() + " / " + item.getUnidade()));


        TipoMovimento tipo = TipoMovimento.valueOf(dto.tipo().toUpperCase());
        double q = dto.quantidade();

        switch (tipo) {
            case ENTRADA, DEVOLUCAO -> est.setSaldo(est.getSaldo() + q);
            case SAIDA, AJUSTE -> {
                if (est.getSaldo() < q)
                    throw new IllegalStateException("Saldo insuficiente para saída/ajuste.");
                est.setSaldo(est.getSaldo() - q);
            }
            case RESERVADO -> {
                if (est.getSaldo() < q)
                    throw new IllegalArgumentException("Saldo insuficiente para reserva.");
                est.setSaldo(est.getSaldo() - q);
                est.setReservado(est.getReservado() + q);
            }
            case LIBERACAO -> {
                if (est.getReservado() < q)
                    throw new IllegalArgumentException("Reservado insuficiente para liberação.");
                est.setReservado(est.getReservado() - q);
                est.setSaldo(est.getSaldo() + q);
            }
        }

        estoqueRepo.save(est);

        MovimentoEstoque mov = MovimentoEstoque.builder()
                .item(item)
                .tipo(tipo)
                .quantidade(q)
                .dataMovimento(LocalDateTime.now())
                .observacao(dto.observacao())
                .build();

        movRepo.save(mov);
    }

    public List<EstoqueResponseDTO> listarTodos() {
        return estoqueRepo.findAll().stream()
                .map(EstoqueResponseDTO::from)
                .collect(Collectors.toList());
    }

    public EstoqueResponseDTO buscarPorId(Long id) {
        Estoque est = estoqueRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado com ID: " + id));
        return EstoqueResponseDTO.from(est);
    }

    @Transactional
    public void atualizarEstoque(Long id, EstoqueRequestDTO dto) {
        Estoque est = estoqueRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado: " + id));

        Item item = itemRepo.findBySku(dto.sku())
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + dto.sku()));

        // Atualiza campos principais
        est.setItem(item);
        est.setUnidade(dto.unidade());
        est.setSaldo(dto.saldo());
        est.setReservado(dto.reservado());
        est.setPontoReposicao(dto.pontoReposicao());

        estoqueRepo.save(est);
    }

    @Transactional
    public void deletarEstoque(Long id) {
        if (!estoqueRepo.existsById(id)) {
            throw new IllegalArgumentException("Estoque não encontrado para exclusão: " + id);
        }
        estoqueRepo.deleteById(id);
    }

    // Buscar por unidade
    public List<EstoqueResponseDTO> listarPorUnidade(String unidade) {
        return estoqueRepo.findByUnidade(unidade).stream()
                .map(EstoqueResponseDTO::from)
                .collect(Collectors.toList());
    }

    // Buscar por SKU
    public List<EstoqueResponseDTO> listarPorSku(String sku) {
        return estoqueRepo.findByItemSkuContainingIgnoreCase(sku).stream()
                .map(EstoqueResponseDTO::from)
                .collect(Collectors.toList());
    }
}
