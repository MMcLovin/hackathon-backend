# Hackathon-backend Caixa - API de Simulação de Crédito

Este projeto é uma API de simulação de crédito desenvolvida em Java com Spring Boot, como parte do desafio técnico de Back-end. A API permite simular empréstimos, consultar um catálogo de produtos de crédito, registrar dados de telemetria e gerenciar o histórico de simulações.

## Tecnologias Utilizadas

O seguinte conjunto de ferramentas e tecnologias foi usado para implementar os requisitos do desafio:

* **Linguagem:** Java 17
* **Framework:** Spring Boot
* **Banco de Dados Local:** PostgreSQL (para persistência do histórico e telemetria)
* **Banco de Dados Remoto:** SQL Server (para consultar produtos de crédito)
* **Event Bus:** Azure Event Hubs (para enviar os resultados da simulação)
* **Ferramenta de Build:** Maven
* **Containerização:** Docker e Docker Compose
* **Outros:** Lombok, Hibernate Types, Postman, Git

## Funcionalidades e Endpoints

A aplicação expõe os seguintes endpoints, conforme os requisitos do desafio:

---

### 1. `GET /api/produtos`
Retorna a lista completa de produtos de crédito disponíveis no banco de dados remoto.

**Exemplo de Resposta (Status 200 OK)**
```json
[
  {
    "coProduto": 1,
    "noProduto": "Produto 1",
    "pcTaxaJuros": 0.0179,
    "nuMinimoMeses": 0,
    "nuMaximoMeses": 24,
    "vrMinimo": 200.00,
    "vrMaximo": 10000.00
  },
  {
    "coProduto": 2,
    "noProduto": "Produto 2",
    "pcTaxaJuros": 0.0175,
    "nuMinimoMeses": 25,
    "nuMaximoMeses": 48,
    "vrMinimo": 10001.00,
    "vrMaximo": 100000.00
  }
]
```

-----

### 2. `POST /api/simulacao`

Realiza a simulação de empréstimo. A lógica de negócio valida os parâmetros de entrada em relação aos produtos disponíveis no banco de dados remoto, calcula os planos de amortização SAC e PRICE, persiste a simulação no banco de dados local e envia o resultado para o Event Hub.

**Exemplo de Requisição**

```json
{
  "valorDesejado": 900.00,
  "prazo": 5
}
```

**Exemplo de Resposta (Status 200 OK)**

```json
{
  "idSimulacao": 20180702,
  "codigoProduto": 1,
  "descricaoProduto": "Produto 1",
  "taxaJuros": 0.0179,
  "resultadoSimulacao": [
    {
      "tipo": "SAC",
      "parcelas": [
        {
          "numero": 1,
          "valorAmortizacao": 180.00,
          "valorJuros": 16.11,
          "valorPrestacao": 196.11
        },
        {
          "numero": 2,
          "valorAmortizacao": 180.00,
          "valorJuros": 12.89,
          "valorPrestacao": 192.89
        }
      ]
    },
    {
      "tipo": "PRICE",
      "parcelas": [
        {
          "numero": 1,
          "valorAmortizacao": 173.67,
          "valorJuros": 16.11,
          "valorPrestacao": 189.78
        },
        {
          "numero": 2,
          "valorAmortizacao": 176.78,
          "valorJuros": 13.00,
          "valorPrestacao": 189.78
        }
      ]
    }
  ]
}
```

-----

### 3. `GET /api/simulacao/historico`

Retorna todas as simulações realizadas, com paginação. O endpoint aceita os parâmetros de consulta `page` e `size`.

**Exemplo de Resposta (Status 200 OK)**

```json
{
  "pagina": 1,
  "qtdRegistros": 404,
  "qtdRegistrosPagina": 200,
  "registros": [
    {
      "idSimulacao": 20180702,
      "valorDesejado": 900.00,
      "prazo": 5,
      "valorTotalParcelas": 1243.28
    }
  ]
}
```

-----

### 4. `GET /api/simulacao/volume`

Retorna os valores simulados para cada produto em um dia específico, aceitando `dataReferencia` como um parâmetro de consulta.

**Exemplo de Resposta (Status 200 OK)**

```json
{
  "dataReferencia": "2025-07-30",
  "simulacoes": [
    {
      "codigoProduto": 1,
      "descricaoProduto": "Produto 1",
      "taxaMediaJuros": 0.189,
      "valorMedioPrestacao": 300.00,
      "valorTotalDesejado": 12047.47,
      "valorTotalCredito": 16750.00
    }
  ]
}
```

-----

### 5. `GET /api/simulacao/telemetria`

Retorna dados de telemetria da aplicação (do dia corrente), incluindo o número de requisições, tempo de execução (mínimo, máximo e médio) e percentual de sucesso para cada endpoint.

**Exemplo de Resposta (Status 200 OK)**

```json
{
  "dataReferencia": "2025-07-30",
  "listaEndpoints": [
    {
      "nomeApi": "Simulacao",
      "qtdRequisicoes": 135,
      "tempoMedio": 150.0,
      "tempoMinimo": 23.0,
      "tempoMaximo": 860.0,
      "percentualSucesso": 0.98
    }
  ]
}
```

## Observações

Algumas coisas foram inferidas com base no enunciado do desafio:

* Para o endpoint de volume de simulações por produto, os cálculos foram realizados utilizando o sistema de amortização de menor valor final.
* A mesma lógica foi usada para o endpoint de histórico das simulações, ao somar o valor total das parcelas.
* O endpoint de telemetria retorna apenas as estatísticas das requisições processadas no mesmo dia, ao invés de todas as requisições já realizadas.

## Como Executar o Projeto 

Para rodar a aplicação em um ambiente de container, você precisa ter o Docker e o Docker Compose instalados.

1.  **Configurar Variáveis de Ambiente**: Crie um arquivo `.env` na raiz do projeto (o arquivo .env foi incluído apenas para o envio do projeto *zipado*) e preencha-o com as suas credenciais. O arquivo `.env` fornecido como exemplo já contém as chaves necessárias.

    ```text
    # Credenciais do SQL Server (banco de dados remoto)
    SPRING_DATASOURCE_SQLSERVER_URL=...
    SPRING_DATASOURCE_SQLSERVER_USERNAME=...
    SPRING_DATASOURCE_SQLSERVER_PASSWORD=...
    
    # Credenciais do PostgreSQL (banco de dados local)
    POSTGRES_DB=...
    SPRING_DATASOURCE_POSTGRESQL_URL=...
    SPRING_DATASOURCE_POSTGRESQL_USERNAME=...
    SPRING_DATASOURCE_POSTGRESQL_PASSWORD=...
    
    # Credenciais do Event Hub
    AZURE_EVENTHUB_CONNECTION_STRING='...'
    AZURE_EVENTHUB_NAME=...
    ```

    > **Nota:** As credenciais para o SQL Server e Event Hub foram especificadas no enunciado do desafio.

2.  **Construir e Executar os Containers**: A aplicação será executada em conjunto com um container do PostgreSQL para o banco de dados local. A imagem do PostgreSQL é configurada com as variáveis de ambiente do arquivo `.env`. O Dockerfile também se encarrega de executar o `mvn clean package` (pulando os testes de integração, uma vez que tentam acessar contextos que apenas são configurados ao executar a aplicação pelo Docker), gerando o `.jar` da aplicação que é copiado para o container apropriado.

    ```sh
    docker compose up --build
    ```

3.  **Acessar a Aplicação**: A aplicação estará disponível na porta `8080` do seu host.

    ```
    http://localhost:8080
    ```