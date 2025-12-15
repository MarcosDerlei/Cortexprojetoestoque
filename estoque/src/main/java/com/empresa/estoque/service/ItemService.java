package com.empresa.estoque.service;

import com.empresa.estoque.dto.ItemRequestDTO;
import com.empresa.estoque.dto.ItemResponseDTO;
import com.empresa.estoque.dto.ItemUpdateDTO;
import com.empresa.estoque.model.CategoriaItem;
import com.empresa.estoque.model.Estoque;
import com.empresa.estoque.model.Item;
import com.empresa.estoque.model.SubcategoriaItem;
import com.empresa.estoque.repository.CategoriaItemRepository;
import com.empresa.estoque.repository.EstoqueRepository;
import com.empresa.estoque.repository.ItemRepository;
import com.empresa.estoque.repository.SubcategoriaItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service

public class ItemService {

    private final SubcategoriaItemRepository subcatRepo;
    private final CategoriaItemRepository categoriaRepo;
    private final ItemRepository itemRepo;
    private final EstoqueRepository estRepo;

    public ItemService(ItemRepository itemRepo, EstoqueRepository estRepo, CategoriaItemRepository categoriaRepo, SubcategoriaItemRepository subcatRepo) {
        this.itemRepo = itemRepo;
        this.estRepo = estRepo;
        this.categoriaRepo = categoriaRepo;
        this.subcatRepo = subcatRepo;
    }

    @Transactional
    public ItemResponseDTO criar(ItemRequestDTO dto) {
        if (itemRepo.existsBySku(dto.sku())) {
            throw new IllegalArgumentException("SKU já existe: " + dto.sku());
        }

        CategoriaItem categoria = categoriaRepo.findById(dto.categoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + dto.categoriaId()));

        SubcategoriaItem subcat = null;
        if (dto.subcategoriaId() != null) {
            subcat = subcatRepo.findById(dto.subcategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Subcategoria não encontrada: " + dto.subcategoriaId()));
        }

        Item item = Item.builder()
                .sku(dto.sku())
                .descricao(dto.descricao())
                .categoria(categoria)
                .subcategoria(subcat)
                .unidade(dto.unidade())
                .custoUnitario(dto.custoUnitario())
                .fornecedor(dto.fornecedor())
                .localizacao(dto.localizacao())
                .observacao(dto.observacao())
                .ativo(true)
                .build();

        item = itemRepo.save(item);

        Estoque est = Estoque.builder()
                .item(item)
                .unidade(item.getUnidade())
                .saldo(dto.quantidadeInicial() == null ? 0.0 : dto.quantidadeInicial())
                .reservado(0.0)
                .pontoReposicao(dto.pontoReposicao() == null ? 0.0 : dto.pontoReposicao())
                .build();

        est = estRepo.save(est);

        return toDTO(item, est);
    }


    public List<ItemResponseDTO> listar() {
        return itemRepo.findByAtivoTrue().stream()
                .map(it -> toDTO(it, estRepo.findByItem(it).orElseThrow()))
                .toList();
    }


    public List<ItemResponseDTO> buscar(String termo) {
        return itemRepo.findByDescricaoContainingIgnoreCaseOrSkuContainingIgnoreCase(termo, termo)
                .stream()
                .map(it -> toDTO(it, estRepo.findByItem(it).orElseThrow()))
                .toList();
    }

    public List<ItemResponseDTO> listarPorCategoria(Long categoriaId) {
        return itemRepo.findByCategoriaId(categoriaId)
                .stream()
                .map(it -> toDTO(it, estRepo.findByItem(it).orElseThrow()))
                .toList();
    }

    public List<ItemResponseDTO> listarPorSubcategoria(Long subcatId) {
        return itemRepo.findBySubcategoriaId(subcatId)
                .stream()
                .map(it -> toDTO(it, estRepo.findByItem(it).orElseThrow()))
                .toList();
    }

    public ItemResponseDTO detalhar(String sku) {
        Item item = itemRepo.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("SKU não encontrado: " + sku));

        Estoque est = estRepo.findByItem(item)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado para SKU: " + sku));

        return toDTO(item, est);
    }

    @Transactional
    public ItemResponseDTO atualizar(String sku, ItemUpdateDTO dto) {

        Item item = itemRepo.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("SKU não encontrado: " + sku));

        // Atualiza categoria somente se enviada
        if (dto.categoriaId() != null) {
            CategoriaItem categoria = categoriaRepo.findById(dto.categoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + dto.categoriaId()));
            item.setCategoria(categoria);
        }

        // Atualiza subcategoria somente se enviada
        if (dto.subcategoriaId() != null) {
            SubcategoriaItem subcat = subcatRepo.findById(dto.subcategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Subcategoria não encontrada: " + dto.subcategoriaId()));
            item.setSubcategoria(subcat);
        }

        // Atualiza campos simples se vieram no DTO
        if (dto.descricao() != null) item.setDescricao(dto.descricao());
        if (dto.unidade() != null) item.setUnidade(dto.unidade());
        if (dto.custoUnitario() != null) item.setCustoUnitario(dto.custoUnitario());
        if (dto.fornecedor() != null) item.setFornecedor(dto.fornecedor());
        if (dto.localizacao() != null) item.setLocalizacao(dto.localizacao());
        if (dto.observacao() != null) item.setObservacao(dto.observacao());
        if (dto.ativo() != null) item.setAtivo(dto.ativo());

        // Atualização no item
        item = itemRepo.save(item);

        // Estoque
        Estoque est = estRepo.findByItem(item).orElseThrow();

        if (dto.pontoReposicao() != null) {
            est.setPontoReposicao(dto.pontoReposicao());
            estRepo.save(est);
        }

        return toDTO(item, est);
    }

    @Transactional
    public void desativar(String sku) {
        Item item = itemRepo.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("SKU não encontrado: " + sku));

        item.setAtivo(false);
        itemRepo.save(item);
    }

    private ItemResponseDTO toDTO(Item item, Estoque est) {
        return new ItemResponseDTO(
                item.getId(),
                item.getSku(),
                item.getDescricao(),

                item.getCategoria().getId(),
                item.getCategoria().getNome(),

                item.getSubcategoria() == null ? null : item.getSubcategoria().getId(),
                item.getSubcategoria() == null ? null : item.getSubcategoria().getNome(),

                item.getUnidade(),
                est.getSaldo(),
                est.getReservado(),
                est.getPontoReposicao(),
                item.getCustoUnitario(),

                item.isAtivo(),
                calcularAlerta(est.getSaldo(), est.getPontoReposicao()),

                item.getFornecedor(),
                item.getLocalizacao(),
                item.getObservacao(),

                item.getUltimaAtualizacao()
        );

    }

    public String calcularAlerta(Double saldo, Double ponto) {
        if (saldo == null || ponto == null) return "DESCONHECIDO";
        if (saldo <= 1) return "CRÍTICO";
        if (saldo <= 3) return "ALTO";
        if (saldo <= 5) return "ATENÇÃO";
        if (saldo <= ponto) return "ABAIXO DO PONTO";
        return "OK";
    }
}
