package com.csvapi.leitordecsv.repository;

import com.csvapi.leitordecsv.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Optional<Produto> findByNome(String nome);

    Optional<Produto> findByCodigoExterno(String s);
}