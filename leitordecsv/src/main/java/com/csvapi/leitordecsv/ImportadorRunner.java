package com.csvapi.leitordecsv;

import com.csvapi.leitordecsv.service.CsvImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ImportadorRunner implements CommandLineRunner {
    private final CsvImportService csvImportService;

    public ImportadorRunner(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Iniciando a importação do CSV...");
        csvImportService.LerCSV();
        System.out.println("Importação do CSV concluída.");
    }
}
