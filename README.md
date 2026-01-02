# 📊 CSV Data Importer - Amazon Sales Analysis

O projeto consiste em um software capaz de ler o arquivo CSV proposto (`Amazon.csv`), separá-lo em tabelas com o auxílio de **Models** (`Cliente`, `ItemVenda`, `Produto` e `Venda`) e persistir os dados no banco de dados `amazon_db`.

## 🚀 Fluxo do Projeto

Dentro deste software, usamos a **leitura em lote (batch)**: a cada 1000 linhas, o sistema processa em blocos tudo o que leu, otimizando a performance. Para encaixarmos os dados corretamente, usamos as entidades que representam nossas tabelas:

* **Cliente:** Guarda quem fez a compra.
* **Produto:** Guarda o que foi vendido, incluindo categoria e marca.
* **Venda:** Onde fica registrado o pedido (cabeçalho com data e valor total).
* **ItemVenda:** Como uma venda pode ter vários produtos, o ItemVenda separa e detalha cada produto (quantidade e preço no momento) de uma venda específica.

### 🛠️ Persistência e Inteligência

* **Repositórios:** Interfaces que conversam com o banco usando **Spring Data JPA**. Ele já nos entrega comandos prontos como `saveAll()` (salvar lista) e `deleteAllInBatch()` (limpar tabela rápido).
* **Queries Customizadas (@Query):** Criamos comandos em SQL puro para realizar cálculos de faturamento e rankings diretamente no banco de dados.
* **Records (DTOs):** Objetos leves para transferir apenas os dados necessários para os relatórios, deixando a API mais rápida.

---

## 🏗️ Arquitetura do Serviço (`CsvImportService`)

Abaixo, detalho exatamente o que fiz em cada linha do código, explicando a lógica por trás de cada comando:

### 1. Preparação e Injeção

* **`@PersistenceContext EntityManager`:** É o nosso "mestre de obras". Ele dá uma conexão direta com o banco. Uso ele no final para dar um `flush` (empurrar dados) e `clear` (limpar memória), evitando que o servidor fique lento.
* **Construtor e Repositórios:** Uso o `private final` para garantir que os repositórios não mudem. O Spring percebe que o serviço precisa dessas 4 ferramentas para "nascer" e as injeta automaticamente.
* **Constantes (`CSV_FILE_NAME` e `BATCH_SIZE`):** Configurações globais. Se o arquivo mudar de nome ou o servidor aguentar mais de 1000 linhas por vez, basta alterar aqui.

### 2. O Método `LerCSV()` - O Coração do Código

#### **A Faxina Inicial**

```java
itemVendaRepository.deleteAllInBatch(); // Apaga itens primeiro

```

* **O que faz:** Antes de começar, limpamos o banco. Como as tabelas são ligadas, apago os itens primeiro para o banco não reclamar. É o nosso "Truncate" para começar a importação do zero.

#### **Mapas e Buffers (Memória)**

```java
Map<String, Cliente> mapaClientes = new HashMap<>(); // Memória de reconhecimento
List<Cliente> bufferClientes = new ArrayList<>();   // O Caminhão

```

* **Mapas:** Funcionam como uma "lista de convidados". Se o cliente "João" já foi lido, ele está no mapa. Assim, não criamos duplicatas no banco.
* **Listas (Buffers):** Se o arquivo é a "areia", os buffers são os **caminhões**. Guardamos os objetos aqui até atingir o limite do lote.

#### **Abertura e Início da Leitura**

```java
try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE_NAME))) {
    String[] line; // A Caçamba
    int count = 0; // O Odômetro
    reader.readNext(); // Pulo do cabeçalho

```

* **Try-with-Resources:** Garante que o arquivo seja fechado sozinho se der erro ou terminar.
* **`line` (Caçamba):** Recipiente temporário que guarda uma linha por vez.
* **`count`:** Conta até 1000 para sabermos quando descarregar o lote.
* **`readNext()`:** Pula a primeira linha do CSV (onde ficam os títulos das colunas).

#### **O Laço de Repetição e Resiliência**

```java
while ((line = reader.readNext()) != null) {
    try { ... } catch (Exception e) { ... }
}

```

* **`while`:** O motor que gira até o arquivo acabar.
* **`try-catch` interno:** É o nosso amortecedor. Se a linha 500 tiver um erro, o `catch` segura, avisa no log e o `while` continua para a linha 501. O sistema é resiliente e não trava.

#### **A Peneira (Filtro e Conversão)**

```java
String clienteNome = line[3].trim();
Double preco = Double.parseDouble(line[9].trim().replace(",", "."));
LocalDate dataVenda = LocalDate.parse(line[1].trim());

```

* **`line[X]`:** Pega a coluna exata do CSV.
* **`.trim()`:** Remove espaços em branco invisíveis.
* **`Double.parseDouble` e `LocalDate.parse`:** Transformam texto em "número de verdade" e "data de verdade" para podermos fazer contas e relatórios.

#### **Lógica de Deduplicação e Soma**

```java
if (cliente == null) { ... mapaClientes.put(...); bufferClientes.add(...); }
venda.setValorTotal(venda.getValorTotal() + (preco * quantidade));

```

* **Deduplicação:** Se o mapa disser que o cliente é novo (`null`), criamos um. Se já existir, usamos o antigo. Isso mantém o banco limpo.
* **Contabilidade:** Somamos o (preço x quantidade) ao valor atual da venda. Se a venda tem 3 produtos, o sistema vai acumulando até ter o valor final da compra.

#### **Fechamento do Pacote (Batch)**

```java
count++;
if (count % BATCH_SIZE == 0) {
    processarLote(...);
}

```

* **Gatilho:** Quando o contador bate 1000, chamamos o `processarLote`, que dá o `saveAll()` nos caminhões e limpa as listas para o próximo ciclo.
* **Finalização:** No fim do `while`, se sobrou algo no caminhão (ex: as últimas 500 linhas), o código faz um último salvamento para não perder nada.

---

## 📦 Tecnologias e Dependências

* **Java 17 & Spring Boot 3**
* **Maven** (Gerenciador de dependências)
* **MySQL Connector** (Conexão com banco)
* **OpenCSV** (Leitor inteligente de arquivos)

---

### Como rodar o projeto:

1. Certifique-se de que o arquivo `Amazon.csv` está na raiz do projeto.
2. Configure o seu banco de dados no `application.properties`.
3. Execute o comando `./mvnw spring-boot:run`.
4. Acesse o endpoint de importação para iniciar o processo.

---