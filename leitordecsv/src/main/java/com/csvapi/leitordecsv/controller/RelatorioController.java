package com.csvapi.leitordecsv.controller;

import com.csvapi.leitordecsv.dto.CategoriaFaturamentoDTO;
import com.csvapi.leitordecsv.dto.MarcaFaturamentoDTO;
import com.csvapi.leitordecsv.dto.ProdutoMaisVendidoDTO;
import com.csvapi.leitordecsv.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {
    @Autowired
    private VendaRepository vendaRepository;

    @GetMapping ("/marcas-top10")
    public List<MarcaFaturamentoDTO> getMarcasTop10() {
        return vendaRepository.buscarTop10Marcas();
    }

    @GetMapping("/categorias")
    public List<CategoriaFaturamentoDTO> getCategorias() {
        return vendaRepository.faturamentoPorCategoria();
    }

    @GetMapping("/produtos-campeoes")
    public List<ProdutoMaisVendidoDTO> getProdutosCampeoes() {
        return vendaRepository.buscarProdutosMaisVendidosPorCategoria();
    }
}
