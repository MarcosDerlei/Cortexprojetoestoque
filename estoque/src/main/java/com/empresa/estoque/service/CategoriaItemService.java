package com.empresa.estoque.service;

import com.empresa.estoque.model.CategoriaItem;
import com.empresa.estoque.repository.CategoriaItemRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service

public class CategoriaItemService {

    private final CategoriaItemRepository repo;

    public CategoriaItemService(CategoriaItemRepository repo) {
        this.repo = repo;
    }

    public CategoriaItem salvar(CategoriaItem categoria) {
        return repo.save(categoria);
    }

    public List<CategoriaItem> listarAtivos() {
        return repo.findAll()
                .stream()
                .filter(CategoriaItem::isAtivo)
                .toList();
    }

    public CategoriaItem buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
    }

    public CategoriaItem atualizar(Long id, CategoriaItem categoriaAtualizada) {
        CategoriaItem existente = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        existente.setNome(categoriaAtualizada.getNome());
        existente.setDescricao(categoriaAtualizada.getDescricao());
        existente.setAtivo(categoriaAtualizada.isAtivo());
        existente.setCodigo(categoriaAtualizada.getCodigo());

        return repo.save(existente);
    }

    public void deletar(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Categoria não encontrada");
        }
        repo.deleteById(id);
    }
}
