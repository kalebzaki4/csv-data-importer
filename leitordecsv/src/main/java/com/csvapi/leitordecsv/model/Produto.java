package com.csvapi.leitordecsv.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column ( unique = true )
    private String codigoExterno;

    @Column
    private String nome;

    @Column
    private String categoria;

    @Column
    private String marca;

}