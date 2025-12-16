package com.csvapi.leitordecsv;

import com.csvapi.leitordecsv.service.CsvImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LeitordecsvApplication implements CommandLineRunner {

    private final CsvImportService csvImportService;

    public LeitordecsvApplication(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    public static void main(String[] args) {
        SpringApplication.run(LeitordecsvApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        this.csvImportService.runImportLogic(args);
    }
}