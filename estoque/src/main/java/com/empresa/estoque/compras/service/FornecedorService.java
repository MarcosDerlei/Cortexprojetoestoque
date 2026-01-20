package com.empresa.estoque.compras.service;

import com.empresa.estoque.compras.dto.FornecedorRequestDTO;
import com.empresa.estoque.compras.dto.FornecedorResponseDTO;
import com.empresa.estoque.compras.model.Fornecedor;
import com.empresa.estoque.compras.repository.FornecedorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public List<FornecedorResponseDTO> listar(Boolean somenteAtivos) {
        List<Fornecedor> fornecedores;

        if (somenteAtivos != null && somenteAtivos) {
            fornecedores = fornecedorRepository.findByAtivoTrueOrderByNomeAsc();
        } else {
            fornecedores = fornecedorRepository.findAll();
        }

        return fornecedores.stream().map(this::toDTO).toList();
    }

    public FornecedorResponseDTO buscarPorId(Long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado."));

        return toDTO(fornecedor);
    }

    public FornecedorResponseDTO criar(FornecedorRequestDTO dto) {
        if (fornecedorRepository.existsByNomeIgnoreCase(dto.nome())) {
            throw new RuntimeException("Já existe um fornecedor com este nome.");
        }

        Fornecedor fornecedor = Fornecedor.builder()
                .nome(dto.nome())
                .whatsapp(dto.whatsapp())
                .ativo(dto.ativo() == null ? true : dto.ativo())
                .build();

        return toDTO(fornecedorRepository.save(fornecedor));
    }

    public FornecedorResponseDTO atualizar(Long id, FornecedorRequestDTO dto) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado."));

        // valida duplicado se trocar nome
        if (!fornecedor.getNome().equalsIgnoreCase(dto.nome())
                && fornecedorRepository.existsByNomeIgnoreCase(dto.nome())) {
            throw new RuntimeException("Já existe um fornecedor com este nome.");
        }

        fornecedor.setNome(dto.nome());
        fornecedor.setWhatsapp(dto.whatsapp());

        if (dto.ativo() != null) {
            fornecedor.setAtivo(dto.ativo());
        }

        return toDTO(fornecedorRepository.save(fornecedor));
    }

    public void deletar(Long id) {
        if (!fornecedorRepository.existsById(id)) {
            throw new EntityNotFoundException("Fornecedor não encontrado.");
        }

        fornecedorRepository.deleteById(id);
    }

    private FornecedorResponseDTO toDTO(Fornecedor fornecedor) {
        return new FornecedorResponseDTO(
                fornecedor.getId(),
                fornecedor.getNome(),
                fornecedor.getWhatsapp(),
                fornecedor.isAtivo()
        );
    }
}
