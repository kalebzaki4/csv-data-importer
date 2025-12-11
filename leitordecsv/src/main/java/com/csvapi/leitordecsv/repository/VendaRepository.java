package com.csvapi.leitordecsv.repository;

import com.csvapi.leitordecsv.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    Optional<Venda> findByCodigoExterno(String codigoExterno);
}