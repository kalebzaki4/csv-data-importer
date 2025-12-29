package com.csvapi.leitordecsv.service;

import com.csvapi.leitordecsv.model.*;
import com.csvapi.leitordecsv.repository.*;
import com.opencsv.CSVReader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.time.LocalDate;
import java.util.*;

@Service
public class CsvImportService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemVendaRepository itemVendaRepository;

    public CsvImportService(ClienteRepository clienteRepository, VendaRepository vendaRepository,
                            ProdutoRepository produtoRepository, ItemVendaRepository itemVendaRepository) {
        this.clienteRepository = clienteRepository;
        this.vendaRepository = vendaRepository; 
        this.produtoRepository = produtoRepository;
        this.itemVendaRepository = itemVendaRepository;
    }

    private static final String CSV_FILE_NAME = "Amazon.csv";
    private static final int BATCH_SIZE = 1000;

    @Transactional
    public void LerCSV() {

        itemVendaRepository.deleteAllInBatch(); // Apaga itens primeiro
        vendaRepository.deleteAllInBatch();
        produtoRepository.deleteAllInBatch();
        clienteRepository.deleteAllInBatch();

        Map<String, Cliente> mapaClientes = new HashMap<>();
        Map<String, Produto> mapaProdutos = new HashMap<>();
        Map<String, Venda> mapaVendas = new HashMap<>();

        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE_NAME))) {
            String[] line;
            int count = 0;
            reader.readNext();

            List<Cliente> bufferClientes = new ArrayList<>();
            List<Produto> bufferProdutos = new ArrayList<>();
            List<Venda> bufferVendas = new ArrayList<>();
            List<ItemVenda> bufferItens = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                try {
                    String vendaId = line[0].trim();
                    String clienteNome = line[3].trim();
                    String produtoNome = line[5].trim();
                    String categoria = line[6].trim();
                    String marca = line[7].trim();
                    Double preco = Double.parseDouble(line[9].trim().replace(",", "."));
                    Integer quantidade = Integer.parseInt(line[8].trim());

                    String dataStr = line[1].trim();
                    LocalDate dataVenda = LocalDate.parse(dataStr);


                    Cliente cliente = mapaClientes.get(clienteNome);
                    if (cliente == null) {
                        cliente = new Cliente();
                        cliente.setNome(clienteNome);
                        mapaClientes.put(clienteNome, cliente);
                        bufferClientes.add(cliente);
                    }

                    Produto produto = mapaProdutos.get(produtoNome);
                    if (produto == null) {
                        produto = new Produto();
                        produto.setNome(produtoNome);
                        produto.setMarca(marca);
                        produto.setCategoria(categoria);
                        mapaProdutos.put(produtoNome, produto);
                        bufferProdutos.add(produto);
                    }

                    Venda venda = mapaVendas.get(vendaId);
                    if (venda == null) {
                        venda = new Venda();
                        venda.setCliente(cliente);

                        venda.setDataVenda(dataVenda);
                        venda.setValorTotal(0.0);

                        mapaVendas.put(vendaId, venda);
                        bufferVendas.add(venda);
                    }

                    venda.setValorTotal(venda.getValorTotal() + (preco * quantidade));

                    ItemVenda itemVenda = new ItemVenda();
                    itemVenda.setVenda(venda);
                    itemVenda.setProduto(produto);
                    itemVenda.setPrecoUnitario(preco);
                    itemVenda.setQuantidade(quantidade);
                    bufferItens.add(itemVenda);

                    // Só vai se a linha inteira deu certo
                    count++;

                    // Salvamento em lote
                    if (count % BATCH_SIZE == 0) {
                        processarLote(bufferClientes, bufferProdutos, bufferVendas, bufferItens);
                    }

                } catch (Exception e) {
                    System.err.println("Erro ao processar linha: " + Arrays.toString(line) + " | Erro: " + e.getMessage());
                }
            }

            // Finalização dos registros restantes
            if (!bufferItens.isEmpty()) {
                processarLote(bufferClientes, bufferProdutos, bufferVendas, bufferItens);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processarLote(List<Cliente> bc, List<Produto> bp, List<Venda> bv, List<ItemVenda> bi) {
        clienteRepository.saveAll(bc);
        produtoRepository.saveAll(bp);
        vendaRepository.saveAll(bv);
        itemVendaRepository.saveAll(bi);

        bc.clear();
        bp.clear();
        bv.clear();
        bi.clear();

        entityManager.flush();
        entityManager.clear();
    }
}