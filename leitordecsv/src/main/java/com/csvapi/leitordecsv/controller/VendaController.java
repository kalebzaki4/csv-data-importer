package com.csvapi.leitordecsv.controller;

import com.csvapi.leitordecsv.model.Venda;
import com.csvapi.leitordecsv.repository.ItemVendaRepository;
import com.csvapi.leitordecsv.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {
    @Autowired
    private VendaRepository vendaRepository;

    @GetMapping
    public List<Venda> BusccarTodasVendas() {
        return vendaRepository.findAll();
    }

    @GetMapping("/cliente-vip")
    public String getClienteVip() {
        return vendaRepository.buscarClienteQueMaisGastou();
    }


}
