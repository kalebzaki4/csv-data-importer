package com.csvapi.leitordecsv.repository;

import com.csvapi.leitordecsv.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Optional<Produto> findByNome(String nome);

    Optional<Produto> findByCodigoExterno(String s);

    boolean existsByCodigoExterno(String codigoProduto);

    @Query("SELECT p.codigoExterno FROM Produto p")
    List<String> findCodigoExterno();

    List<Produto> findAll();
}