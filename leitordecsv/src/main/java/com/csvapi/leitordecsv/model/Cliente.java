package com.csvapi.leitordecsv.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Cliente {
    @Id
    @GeneratedValue(
            strategy = GenerationType.TABLE,
            generator = "cliente_table_generator"

    )
    @TableGenerator(
            name = "cliente_table_generator",
            table = "id_generator",
            pkColumnName = "entity_name",
            valueColumnName = "next_val",
            pkColumnValue = "cliente",
            allocationSize = 50
    )
    private Long id;

    private String nome;

    private String email;

    @Column (unique = true)
    private String codigoExterno;
}