package com.csvapi.leitordecsv.service;

import com.csvapi.leitordecsv.model.Cliente;
import com.csvapi.leitordecsv.model.ItemVenda;
import com.csvapi.leitordecsv.model.Produto;
import com.csvapi.leitordecsv.model.Venda;
import com.csvapi.leitordecsv.repository.ClienteRepository;
import com.csvapi.leitordecsv.repository.ItemVendaRepository;
import com.csvapi.leitordecsv.repository.ProdutoRepository;
import com.csvapi.leitordecsv.repository.VendaRepository;
import com.opencsv.CSVReader;
import io.micrometer.observation.Observation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CsvImportService {

    // importando os repositories via constructor injection
    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemVendaRepository itemVendaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String CSV_FILE_NAME = "Amazon.csv";
    private static final int BATCH_SIZE = 1000;

    public CsvImportService(ClienteRepository clienteRepository, VendaRepository vendaRepository, ProdutoRepository produtoRepository, ItemVendaRepository itemVendaRepository) {
        this.clienteRepository = clienteRepository;
        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
        this.itemVendaRepository = itemVendaRepository;
    }

    public void LerCSV() {
        Set<String> clientesProcessados = new HashSet<>();
        Set<String> produtosProcessados = new HashSet<>();
        Set<String> vendasProcessados = new HashSet<>();

        // Carregar clientes existentes
        List<Cliente> clientes = clienteRepository.findAll();

        Map<String, Cliente> mapaClientes = new HashMap<>();
        Map<String, Produto> mapaProdutos = new HashMap<>();
        Map<String, Venda> mapaVendas = new HashMap<>();

        try (CSVReader reader = new CSVReader(new java.io.FileReader(CSV_FILE_NAME))) {
            String[] line;
            int count = 0;

            reader.readNext();

            List<Cliente> bufferClientes = new ArrayList<>();
            List<Produto> bufferProdutos = new ArrayList<>();
            List<Venda> bufferVendas = new ArrayList<>();
            List<ItemVenda> bufferItens = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                String clienteNome = line[0].trim();
                String produtoNome = line[1].trim();
                String vendaId = line[2].trim();

                Cliente cliente = mapaClientes.get(clienteNome);
                if (cliente == null) {
                    cliente = new Cliente();
                    cliente.setNome(clienteNome);
                    clienteRepository.save(cliente);
                    mapaClientes.put(clienteNome, cliente);

                    bufferClientes.add(cliente);
                    mapaClientes.put(clienteNome, cliente);
                }

                // Processar Cliente
                if (!clientesProcessados.contains(clienteNome)) {
                    Cliente cliente = new Cliente();
                    cliente.setNome(clienteNome);
                    bufferClientes.add(cliente);
                    clientesProcessados.add(clienteNome);
                }

            }

            // Final flush and clear
            entityManager.flush();
            entityManager.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
