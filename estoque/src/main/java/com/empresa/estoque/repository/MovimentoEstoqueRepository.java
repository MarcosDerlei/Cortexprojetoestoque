package com.empresa.estoque.repository;

import com.empresa.estoque.model.Item;
import com.empresa.estoque.model.MovimentoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {

    List<MovimentoEstoque> findByItemOrderByDataMovimentoDesc(Item item);
}
