package com.csvapi.leitordecsv.service;

import com.csvapi.leitordecsv.model.Cliente;
import com.csvapi.leitordecsv.model.Venda;
import com.csvapi.leitordecsv.model.ItemVenda;
import com.csvapi.leitordecsv.model.Produto;
import com.csvapi.leitordecsv.repository.VendaRepository;
import com.csvapi.leitordecsv.repository.ItemVendaRepository;
import com.csvapi.leitordecsv.repository.ProdutoRepository;
import com.csvapi.leitordecsv.repository.ClienteRepository;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class CsvImportService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String CSV_FILE_NAME = "Amazon.csv";
    private static final int BATCH_SIZE = 500;

    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

    public CsvImportService(VendaRepository vendaRepository, ItemVendaRepository itemVendaRepository, ProdutoRepository produtoRepository, ClienteRepository clienteRepository) {
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public void runImportLogic(String... args) throws Exception {
        System.out.println("--- INICIANDO IMPORTAÇÃO CSV ---");

        importarClientesEProdutos();

        importarVendas();

        importarItensVenda();

        System.out.println("--- IMPORTAÇÃO CONCLUÍDA ---");
    }

    private CSVReader getCsvReader(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(fileName);
        if (!resource.exists()) {
            throw new IOException("Arquivo não encontrado: " + fileName);
        }
        return new CSVReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
    }

    @Transactional
    public void importarClientesEProdutos() {
        System.out.println("Importando Clientes e Produtos (Batch Otimizado)...");

        Set<String> clientesExistentes = new HashSet<>(clienteRepository.findCodigoExterno());
        Set<String> produtosExistentes = new HashSet<>(produtoRepository.findCodigoExterno());

        List<Cliente> novosClientes = new ArrayList<>();
        List<Produto> novosProdutos = new ArrayList<>();

        int countClientes = 0;
        int countProdutos = 0;

        try (CSVReader reader = getCsvReader(CSV_FILE_NAME)) {
            String[] nextRecord;
            reader.readNext();

            while ((nextRecord = reader.readNext()) != null) {
                if (nextRecord.length < 6) continue;

                // CLIENTE
                String codigoCliente = nextRecord[2];
                if (!clientesExistentes.contains(codigoCliente)) {
                    Cliente cliente = new Cliente();
                    cliente.setCodigoExterno(codigoCliente);
                    cliente.setNome(nextRecord[3]);

                    novosClientes.add(cliente);
                    clientesExistentes.add(codigoCliente);
                    countClientes++;
                }

                String codigoProduto = nextRecord[4];
                if (!produtosExistentes.contains(codigoProduto)) {
                    Produto produto = new Produto();
                    produto.setCodigoExterno(codigoProduto);
                    produto.setNome(nextRecord[5]);

                    novosProdutos.add(produto);
                    produtosExistentes.add(codigoProduto);
                    countProdutos++;
                }

                if (novosClientes.size() >= BATCH_SIZE) {
                    clienteRepository.saveAll(novosClientes);
                    novosClientes.clear();
                    produtoRepository.saveAll(novosProdutos);
                    novosProdutos.clear();
                    entityManager.flush();
                    entityManager.clear();
                }
            }

            if (!novosClientes.isEmpty()) {
                clienteRepository.saveAll(novosClientes);
            }
            if (!novosProdutos.isEmpty()) {
                produtoRepository.saveAll(novosProdutos);
            }

            entityManager.flush();
            entityManager.clear();

            System.out.println("Importação de Clientes concluída. Total: " + countClientes);
            System.out.println("Importação de Produtos concluída. Total: " + countProdutos);

        } catch (IOException | CsvValidationException e) {
            System.err.println("ERRO fatal ao ler o CSV de Clientes/Produtos: " + e.getMessage());
            throw new RuntimeException("Falha na importação de Clientes/Produtos.", e);
        }
    }

    @Transactional
    public void importarVendas() {
        System.out.println("Iniciando importação de Vendas (Batch Otimizado)...");

        Map<String, Cliente> clientesPorCodigo = new HashMap<>();
        clienteRepository.findAll().forEach(cliente ->
                clientesPorCodigo.put(cliente.getCodigoExterno(), cliente)
        );

        List<Venda> novasVendas = new ArrayList<>();
        int count = 0;

        try (CSVReader reader = getCsvReader(CSV_FILE_NAME)) {
            String[] nextRecord;
            reader.readNext();

            while ((nextRecord = reader.readNext()) != null) {
                try {
                    String codigoCliente = nextRecord[2];
                    Cliente cliente = clientesPorCodigo.get(codigoCliente);

                    if (cliente != null) {
                        Venda venda = new Venda();
                        venda.setCodigoExterno(nextRecord[0]);
                        venda.setDataVenda(LocalDate.parse(nextRecord[1]));
                        venda.setCliente(cliente);

                        novasVendas.add(venda);
                        count++;
                    } else {
                        System.out.println("AVISO VENDA (linha " + reader.getLinesRead() + "): Cliente " + codigoCliente + " não encontrado (Ignorado).");
                    }

                    if (novasVendas.size() >= BATCH_SIZE) {
                        vendaRepository.saveAll(novasVendas);
                        novasVendas.clear();
                        entityManager.flush();
                        entityManager.clear();
                    }

                } catch (ArrayIndexOutOfBoundsException | DateTimeParseException e) {
                    System.err.println("ERRO ao processar Venda na linha " + reader.getLinesRead() + ": " + e.getMessage());
                }
            }

            if (!novasVendas.isEmpty()) {
                vendaRepository.saveAll(novasVendas);
            }

            entityManager.flush();
            entityManager.clear();
            System.out.println("Importação de Vendas concluída. Total: " + count);
        } catch (IOException | CsvValidationException e) {
            System.err.println("ERRO fatal ao ler o CSV de Vendas: " + e.getMessage());
            throw new RuntimeException("Falha na importação de Vendas.", e);
        }
    }

    @Transactional
    public void importarItensVenda() {
        System.out.println("Iniciando importação de Itens de Venda (Batch Otimizado)...");

        Map<String, Venda> vendasPorCodigo = new HashMap<>();
        vendaRepository.findAll().forEach(venda ->
                vendasPorCodigo.put(venda.getCodigoExterno(), venda)
        );

        Map<String, Produto> produtosPorCodigo = new HashMap<>();
        produtoRepository.findAll().forEach(produto ->
                produtosPorCodigo.put(produto.getCodigoExterno(), produto)
        );

        List<ItemVenda> novosItensVenda = new ArrayList<>();
        int count = 0;

        try (CSVReader reader = getCsvReader(CSV_FILE_NAME)) {
            String[] nextRecord;
            reader.readNext();

            while ((nextRecord = reader.readNext()) != null) {
                try {
                    String codigoVenda = nextRecord[0];
                    Venda venda = vendasPorCodigo.get(codigoVenda);

                    String codigoProduto = nextRecord[4];
                    Produto produto = produtosPorCodigo.get(codigoProduto);

                    if (venda != null && produto != null) {
                        ItemVenda item = new ItemVenda();
                        item.setVenda(venda);
                        item.setProduto(produto);

                        item.setQuantidade(Integer.parseInt(nextRecord[8]));
                        item.setPrecoUnitarioNaVenda(Double.parseDouble(nextRecord[9]));

                        novosItensVenda.add(item);
                        count++;
                    } else {
                        if (venda == null) {
                            System.out.println("AVISO ITEMVENDA (linha " + reader.getLinesRead() + "): Venda " + codigoVenda + " não encontrada (Ignorado).");
                        }
                        if (produto == null) {
                            System.out.println("AVISO ITEMVENDA (linha " + reader.getLinesRead() + "): Produto " + codigoProduto + " não encontrado (Ignorado).");
                        }
                    }

                    if (novosItensVenda.size() >= BATCH_SIZE) {
                        itemVendaRepository.saveAll(novosItensVenda);
                        novosItensVenda.clear();
                        entityManager.flush();
                        entityManager.clear();
                    }

                } catch (NumberFormatException e) {
                    System.err.println("ERRO de formatação em linha (ItemVenda) " + reader.getLinesRead() + ": " + e.getMessage());
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("ERRO de índice em linha (ItemVenda) " + reader.getLinesRead() + ": " + e.getMessage());
                }
            }

            if (!novosItensVenda.isEmpty()) {
                itemVendaRepository.saveAll(novosItensVenda);
            }

            entityManager.flush();
            entityManager.clear();

            System.out.println("Importação de Itens de Venda concluída. Total: " + count);
        } catch (IOException | CsvValidationException e) {
            System.err.println("ERRO fatal ao ler o CSV de Itens de Venda: " + e.getMessage());
            throw new RuntimeException("Falha na importação de Itens de Venda.", e);
        }
    }
}