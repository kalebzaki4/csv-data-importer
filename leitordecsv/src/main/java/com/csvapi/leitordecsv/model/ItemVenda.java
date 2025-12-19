package com.csvapi.leitordecsv.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ItemVenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column
    private Integer quantidade;

    @Column
    private Double precoUnitario;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;
}