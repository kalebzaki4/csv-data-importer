package com.csvapi.leitordecsv.service;

import com.csvapi.leitordecsv.model.Venda;
import com.csvapi.leitordecsv.model.ItemVenda;
import com.csvapi.leitordecsv.model.Produto;
import com.csvapi.leitordecsv.repository.VendaRepository;
import com.csvapi.leitordecsv.repository.ItemVendaRepository;
import com.csvapi.leitordecsv.repository.ProdutoRepository;
import com.csvapi.leitordecsv.repository.ClienteRepository;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class CsvImportService implements CommandLineRunner {
    private static final String CSV_FILE_NAME = "Amazon.csv";

    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

    public CsvImportService(VendaRepository vendaRepository, ItemVendaRepository itemVendaRepository,
                            ProdutoRepository produtoRepository, ClienteRepository clienteRepository) {
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- INICIANDO FLUXO DE IMPORTAÇÃO DE CSV ---");

        importarClientesEProdutos();

        importarVendas();

        importarItensVenda();

        System.out.println("--- FLUXO DE IMPORTAÇÃO CONCLUÍDO ---");
    }

    private CSVReader getCsvReader(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(fileName);
        if (!resource.exists()) {
            throw new IOException("Arquivo não encontrado no classpath: " + fileName);
        }
        return new CSVReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
    }

    @Transactional
    public void importarClientesEProdutos() {
        System.out.println("Importando Clientes e Produtos (Implementação Pendente)...");
    }

    @Transactional
    public void importarVendas() {
        System.out.println("Iniciando importação de Vendas (Entidades Pais)...");
        try (CSVReader reader = getCsvReader(CSV_FILE_NAME)) {
            String[] nextRecord;
            reader.readNext();
            int count = 0;

            while ((nextRecord = reader.readNext()) != null) {
                try {
                    Venda venda = new Venda();
                    venda.setCodigoExterno(nextRecord[0]);
                    venda.setDataVenda(LocalDate.parse(nextRecord[1]));

                    vendaRepository.save(venda);
                    count++;
                } catch (ArrayIndexOutOfBoundsException | DateTimeParseException e) {
                    System.err.println("ERRO ao processar Venda na linha " + reader.getLinesRead() + ": " + e.getMessage());
                }
            }
            System.out.println("Importação de Vendas concluída. Total: " + count);
        } catch (IOException | CsvValidationException e) {
            System.err.println("ERRO fatal ao ler o CSV de Vendas: " + e.getMessage());
            throw new RuntimeException("Falha na importação de Vendas.", e);
        }
    }

    @Transactional
    public void importarItensVenda() {
        System.out.println("Iniciando importação de Itens de Venda (Entidades Filhas)...");
        try (CSVReader reader = getCsvReader(CSV_FILE_NAME)) {
            String[] nextRecord;
            reader.readNext();
            int count = 0;

            while ((nextRecord = reader.readNext()) != null) {
                String codigoVenda = nextRecord[0];

                Optional<Venda> vendaOpt = vendaRepository.findByCodigoExterno(codigoVenda);

                if (vendaOpt.isPresent()) {
                    try {
                        Optional<Produto> produtoOpt = produtoRepository.findByCodigoExterno(nextRecord[1]);

                        if (produtoOpt.isPresent()) {
                            ItemVenda item = new ItemVenda();
                            item.setVenda(vendaOpt.get());
                            item.setProduto(produtoOpt.get());
                            item.setQuantidade(Integer.parseInt(nextRecord[2]));
                            item.setPrecoUnitarioNaVenda(Double.parseDouble(nextRecord[3]));

                            itemVendaRepository.save(item);
                            count++;
                        } else {
                            System.out.println("AVISO ITEMVENDA (linha " + reader.getLinesRead() + "): Produto " + nextRecord[1] + " não encontrado. Item ignorado.");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("ERRO de formatação em linha (ItemVenda) " + reader.getLinesRead() + ": " + e.getMessage());
                    }
                } else {
                    System.out.println("AVISO ITEMVENDA (linha " + reader.getLinesRead() + "): Venda " + codigoVenda + " não encontrada. Item ignorado.");
                }
            }
            System.out.println("Importação de Itens de Venda concluída. Total: " + count);
        } catch (IOException | CsvValidationException e) {
            System.err.println("ERRO fatal ao ler o CSV de Itens de Venda: " + e.getMessage());
            throw new RuntimeException("Falha na importação de Itens de Venda.", e);
        }
    }
}