package com.empresa.estoque.compras.service;

import com.empresa.estoque.compras.dto.ItemFornecedorPrecoResponseDTO;
import com.empresa.estoque.compras.dto.ItemFornecedorRequestDTO;
import com.empresa.estoque.compras.dto.ItemFornecedorResponseDTO;
import com.empresa.estoque.compras.dto.ItemFornecedorUpdateDTO;
import com.empresa.estoque.compras.model.Fornecedor;
import com.empresa.estoque.compras.model.ItemFornecedor;
import com.empresa.estoque.compras.repository.FornecedorRepository;
import com.empresa.estoque.compras.repository.ItemFornecedorRepository;
import com.empresa.estoque.model.Item;
import com.empresa.estoque.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemFornecedorService {

    private final ItemFornecedorRepository itemFornecedorRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ItemRepository itemRepository;

    // =====================================================
    // ✅ 1) (FRONT) Lista fornecedores do item (GerarPedido)
    // =====================================================
    public List<ItemFornecedorPrecoResponseDTO> listarFornecedoresDoItem(Long itemId) {

        if (!itemRepository.existsById(itemId)) {
            throw new EntityNotFoundException("Item não encontrado.");
        }

        return itemFornecedorRepository
                .findByItemIdAndAtivoTrueOrderByIdDesc(itemId)
                .stream()
                .filter(v -> v.getFornecedor() != null && v.getFornecedor().isAtivo())
                .map(v -> new ItemFornecedorPrecoResponseDTO(
                        v.getId(),
                        v.getFornecedor().getId(),
                        v.getFornecedor().getNome(),
                        v.getFornecedor().getWhatsapp(),
                        v.getPrecoReferencia(),
                        v.getUnidadeCompra(),
                        v.getObservacao()
                ))
                .toList();
    }


    // =====================================================
    // ✅ 2) Vincular fornecedor ao item (Admin/Cadastro)
    // =====================================================
    public ItemFornecedorResponseDTO vincular(ItemFornecedorRequestDTO dto) {

        if (itemFornecedorRepository.existsByItemIdAndFornecedorId(dto.itemId(), dto.fornecedorId())) {
            throw new RuntimeException("Este fornecedor já está vinculado a este item.");
        }

        Item item = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado."));

        Fornecedor fornecedor = fornecedorRepository.findById(dto.fornecedorId())
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado."));

        ItemFornecedor vinculo = ItemFornecedor.builder()
                .item(item)
                .fornecedor(fornecedor)
                .precoReferencia(dto.precoReferencia())
                .unidadeCompra(dto.unidadeCompra())
                .ativo(dto.ativo() == null ? true : dto.ativo())
                .build();

        return toDTO(itemFornecedorRepository.save(vinculo));
    }

    // =====================================================
    // ✅ 3) Listar vínculos item-fornecedor (tela cadastro)
    // =====================================================
    public List<ItemFornecedorResponseDTO> listarPorItem(Long itemId) {

        if (!itemRepository.existsById(itemId)) {
            throw new EntityNotFoundException("Item não encontrado.");
        }

        return itemFornecedorRepository.findByItemIdOrderByIdDesc(itemId)
                .stream()
                .map(this::toDTO)
                .toList();
    }
    public ItemFornecedorResponseDTO atualizar(Long id, ItemFornecedorUpdateDTO dto) {

        ItemFornecedor vinculo = itemFornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo ItemFornecedor não encontrado."));

        vinculo.setPrecoReferencia(dto.precoReferencia());
        vinculo.setUnidadeCompra(dto.unidadeCompra());
        vinculo.setAtivo(dto.ativo());

        ItemFornecedor salvo = itemFornecedorRepository.save(vinculo);
        return toDTO(salvo);
    }


    // =====================================================
    // ✅ 4) Deletar vínculo
    // =====================================================
    public void deletar(Long id) {
        if (!itemFornecedorRepository.existsById(id)) {
            throw new EntityNotFoundException("Vínculo ItemFornecedor não encontrado.");
        }
        itemFornecedorRepository.deleteById(id);
    }

    // =====================================================
    // Mapper
    // =====================================================
    private ItemFornecedorResponseDTO toDTO(ItemFornecedor v) {
        return new ItemFornecedorResponseDTO(
                v.getId(),

                v.getItem().getId(),
                v.getItem().getSku(),
                v.getItem().getDescricao(),

                v.getFornecedor().getId(),
                v.getFornecedor().getNome(),
                v.getFornecedor().getWhatsapp(),

                v.getPrecoReferencia(),
                v.getUnidadeCompra(),
                v.isAtivo()
        );
    }
}
