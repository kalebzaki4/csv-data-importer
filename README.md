# 🚀 csv-data-importer

## 🎯 Visão Geral do Projeto

Este projeto é um **Serviço de ingestão de dados desenvolvido em Spring Boot**, focado na leitura e persistência transacional de arquivos CSV complexos (dados de Vendas) em um banco de dados MySQL.

Ele resolve o desafio de transformar um único arquivo tabular (`amazon.csv`) em múltiplas entidades relacionadas (Venda, ItemVenda, Produto, Cliente) de forma atômica e segura, utilizando o poder do ecossistema Spring Data JPA.

## 🧱 Arquitetura e Fluxo de Dados

O projeto segue um fluxo de trabalho (workflow) de processamento **Batch** (em lote) altamente eficiente: 

1.  **Inicialização:** O serviço `CsvImportService` é carregado como um `CommandLineRunner`, garantindo que a rotina de importação seja executada **automaticamente** assim que a aplicação Spring Boot for iniciada, mas **somente após** o banco de dados estar pronto (`@DependsOn("entityManagerFactory")`).
2.  **Ordem Transacional:** A importação é executada em três etapas transacionais:
    * `importarClientesEProdutos()` (Garantir que as FKs existam).
    * `importarVendas()` (Entidades-pai).
    * `importarItensVenda()` (Entidades-filho, com validação de FKs para Venda e Produto).
3.  **Mapeamento:** O Hibernate gerencia o mapeamento Objeto-Relacional (ORM), persistindo os dados nas tabelas correspondentes de forma segura.
    
**Fluxo de Injeção de Dados:** `CSV (amazon.csv) -> CsvImportService (CommandLineRunner) -> Repositórios (JPA) -> MySQL (amazon_db)`

## 🛠️ Stack Tecnológico

As seguintes tecnologias foram utilizadas para construir o projeto:

* **Linguagem:** Java 17
* **Framework:** Spring Boot 3.x
* **Persistência:** Spring Data JPA / Hibernate
* **Banco de Dados:** MySQL (configurado via Docker ou instalação local)
* **Leitura CSV:** openCSV
* **Gerenciador de Dependências:** Maven

---

## ⚙️ Como Executar o Projeto (Guia Rápido)

Siga os passos abaixo para clonar e executar a aplicação localmente.

### Pré-requisitos

* JDK 17 ou superior
* Maven 3.6+
* MySQL Server (Rodando localmente ou via Docker)

### 1. Clonar o Repositório

```bash
git clone [https://github.com/Kalebzaki4/csv-data-importer.git](https://github.com/Kalebzaki4/csv-data-importer.git)
cd csv-data-importer