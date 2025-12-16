package com.csvapi.leitordecsv.repository;

import com.csvapi.leitordecsv.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import necessário

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmail(String email);

    Optional<Cliente> findByCodigoExterno(String s);

    boolean existsByCodigoExterno(String codigoCliente);

    @Query("SELECT c.codigoExterno FROM Cliente c")
    List<String> findCodigoExterno();

    List<Cliente> findAll();
}