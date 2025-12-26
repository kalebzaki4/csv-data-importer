package com.csvapi.leitordecsv.repository;

import com.csvapi.leitordecsv.dto.CategoriaFaturamentoDTO;
import com.csvapi.leitordecsv.dto.MarcaFaturamentoDTO;
import com.csvapi.leitordecsv.dto.ProdutoMaisVendidoDTO;
import com.csvapi.leitordecsv.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    @Query(value = "SELECT c.nome FROM cliente c JOIN venda v ON c.id = v.cliente_id " + "GROUP BY c.nome ORDER BY SUM(v.valor_total) DESC LIMIT 1", nativeQuery = true)
    String buscarClienteQueMaisGastou();

    @Query("SELECT new com.csvapi.leitordecsv.dto.MarcaFaturamentoDTO(p.marca, SUM(iv.precoUnitario * iv.quantidade)) " + "FROM ItemVenda iv JOIN iv.produto p " + "GROUP BY p.marca ORDER BY SUM(iv.precoUnitario * iv.quantidade) DESC")
    List<MarcaFaturamentoDTO> buscarTop10Marcas();

    @Query("SELECT new com.csvapi.leitordecsv.dto.CategoriaFaturamentoDTO(p.categoria, SUM(iv.precoUnitario * iv.quantidade)) " + "FROM ItemVenda iv JOIN iv.produto p " + "GROUP BY p.categoria")
    List<CategoriaFaturamentoDTO> faturamentoPorCategoria();

    @Query("SELECT new com.csvapi.leitordecsv.dto.ProdutoMaisVendidoDTO(p.categoria, p.nome, SUM(iv.quantidade)) " +
            "FROM ItemVenda iv JOIN iv.produto p " +
            "GROUP BY p.categoria, p.nome " +
            "ORDER BY p.categoria ASC, SUM(iv.quantidade) DESC")
    List<ProdutoMaisVendidoDTO> buscarProdutosMaisVendidosPorCategoria();
}