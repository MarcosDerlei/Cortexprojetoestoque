package com.empresa.estoque.service;

import com.empresa.estoque.dto.SubcategoriaItemRequestDTO;
import com.empresa.estoque.dto.SubcategoriaItemResponseDTO;
import com.empresa.estoque.model.CategoriaItem;
import com.empresa.estoque.model.SubcategoriaItem;
import com.empresa.estoque.repository.CategoriaItemRepository;
import com.empresa.estoque.repository.SubcategoriaItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service

public class SubcategoriaItemService {

    @Autowired
    private SubcategoriaItemRepository repository;

    @Autowired
    private CategoriaItemRepository categoriaRepository;

    public SubcategoriaItemResponseDTO criar(SubcategoriaItemRequestDTO dto) {

        CategoriaItem categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        SubcategoriaItem s = new SubcategoriaItem();
        s.setNome(dto.nome());
        s.setCategoria(categoria);
        s.setAtivo(true);

        repository.save(s);

        return new SubcategoriaItemResponseDTO(
                s.getId(),
                s.getNome(),
                categoria.getId(),
                s.isAtivo()
        );
    }

    public List<SubcategoriaItemResponseDTO> listarTodas() {
        return repository.findAll()
                .stream()
                .map(s -> new SubcategoriaItemResponseDTO(
                        s.getId(),
                        s.getNome(),
                        s.getCategoria().getId(),
                        s.isAtivo()
                )).toList();
    }

    public List<SubcategoriaItemResponseDTO> listarPorCategoria(Long categoriaId) {
        return repository.findByCategoriaId(categoriaId)
                .stream()
                .map(s -> new SubcategoriaItemResponseDTO(
                        s.getId(),
                        s.getNome(),
                        s.getCategoria().getId(),
                        s.isAtivo()
                )).toList();
    }

    public SubcategoriaItemResponseDTO atualizar(Long id, SubcategoriaItemRequestDTO dto) {

        SubcategoriaItem sub = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategoria não encontrada"));

        if (dto.nome() != null) {
            sub.setNome(dto.nome());
        }

        if (dto.categoriaId() != null) {
            CategoriaItem categoria = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
            sub.setCategoria(categoria);
        }

        if (dto.ativo() != null) {
            sub.setAtivo(dto.ativo());
        }

        repository.save(sub);

        return new SubcategoriaItemResponseDTO(
                sub.getId(),
                sub.getNome(),
                sub.getCategoria().getId(),
                sub.isAtivo()
        );
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Subcategoria não encontrada");
        }
        repository.deleteById(id);
    }
}
