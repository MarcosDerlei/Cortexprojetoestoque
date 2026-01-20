package com.empresa.estoque.compras.repository;

import com.empresa.estoque.compras.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    List<Fornecedor> findByAtivoTrueOrderByNomeAsc();

    Optional<Fornecedor> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}
