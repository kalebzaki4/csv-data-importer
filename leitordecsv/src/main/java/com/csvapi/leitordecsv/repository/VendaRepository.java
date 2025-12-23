package com.csvapi.leitordecsv.repository;

import com.csvapi.leitordecsv.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    @Query(value = "SELECT c.nome FROM cliente c JOIN venda v ON c.id = v.cliente_id " +
            "GROUP BY c.nome ORDER BY SUM(v.valor_total) DESC LIMIT 1", nativeQuery = true)
    String buscarClienteQueMaisGastou();
}