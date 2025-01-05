# Arquitetura do Sistema

A arquitetura do sistema pode ser dividida em duas partes principais: **Simulador de Dados de Sensores** e **Simulador da Arquitetura de Processamento de Dados de Sensores**. Abaixo está a descrição detalhada de cada um dos módulos envolvidos, além das tecnologias usadas.

## 1. Simulador de Dados de Sensores

### 1.1 `prediction-frontend`
Este é o front-end onde o usuário pode inserir os parâmetros necessários para a simulação de dados de sensores. O formulário (ver **Figura 1** abaixo) permite que o usuário forneça informações como o nome do sensor, a unidade de medida e os parâmetros para o modelo ARIMA, como o P, D, e Q, além de configurar o número de previsões e o intervalo de tempo entre elas. O front-end também permite que o usuário selecione um arquivo para carregar.

**Tecnologia Usada:** React.js

![Figura 1 - Formulário do Front-end](file-FT1jEPksAsipEeG4i6rNDF.png)

### 1.2 `config-service`
Este serviço é responsável pela comunicação entre o front-end e os serviços de predição. Ele recebe os parâmetros do usuário e os envia para o serviço de predição adequado.

**Tecnologia Usada:** Spring Boot

### 1.3 `prediction-service`
Este é um serviço em Python (Flask) que utiliza o modelo ARIMA para realizar previsões com base nos dados fornecidos. Ele processa os dados e retorna os resultados para os sistemas subsequentes.

**Tecnologia Usada:** Python, Flask, ARIMA

### 1.4 `data-handler`
O serviço `data-handler` consome os dados gerados pelo `prediction-service` e permite visualizar e atualizar os dados de previsão armazenados.

**Tecnologia Usada:** Spring Boot

---

## 2. Simulador da Arquitetura de Processamento de Dados de Sensores

### 2.1 **Parte Indoor**

### 2.1.1 `production-service`
Este serviço é responsável pelo envio dos dados de previsão para a fila `productionQueue`, que os torna disponíveis para o processamento posterior.

**Tecnologia Usada:** Spring Boot

### 2.1.2 `gateway-capturer`
O `gateway-capturer` consome a fila `productionQueue` e inicia dinamicamente uma quantidade necessária de `capture-services`. Os dados são balanceados entre esses serviços utilizando um mecanismo round-robin.

**Tecnologia Usada:** Spring Boot

### 2.1.3 `Capture-service`
O `capture-service` realiza o processamento inicial dos dados. Ele envia os dados para a fila `sensorDataCaptured` e também coleta métricas de desempenho, enviando essas métricas para a fila `metrics`.

**Tecnologia Usada:** Spring Boot

### 2.2 **Parte Outdoor**

### 2.2.1 `gateway-processor`
O `gateway-processor` consome a fila `sensorDataCaptured` e inicializa uma quantidade necessária de `processor-services`. Ele utiliza um balanceador para distribuir as mensagens entre esses serviços.

**Tecnologia Usada:** Spring Boot

### 2.2.2 `processor-service`
Este serviço realiza o processamento pesado dos dados. Ele utiliza três serviços nativos em C (agregador, compressor e filtro) para tratar os dados. Após o processamento, os dados são enviados para a fila `sensorDataProcessed`. As métricas de desempenho também são coletadas e enviadas para a fila `metrics`.

**Tecnologia Usada:** Spring Boot, C (para processamento de dados)

### 2.2.3 `core-service`
O `core-service` é responsável por pegar os dados da fila `sensorDataProcessed`, armazená-los em um banco de dados histórico e disponibilizar endpoints para consumo posterior.

**Tecnologia Usada:** Spring Boot

### 2.3 **Dashboard de Métricas**

### 2.3.1 `metrics-dashboard`
O `metrics-dashboard` fornece uma interface visual para o acompanhamento das métricas de desempenho dos serviços. Ele permite visualizar o uso de CPU, uso de memória e outras métricas coletadas de forma dinâmica.

**Tecnologia Usada:** React.js, Chart.js

![Figura 2 - Dashboard de Métricas dos Sensores](file-HcLg5rWWVKk16KKkAYEgFC.png)

---

## Fluxo de Dados

### Fluxo Geral

1. O **Simulador de Dados de Sensores** (incluindo `prediction-frontend`, `config-service` e `prediction-service`) recebe os parâmetros do usuário e gera dados de sensores.
2. O **Simulador da Arquitetura de Processamento de Dados de Sensores** processa os dados gerados, começando pela parte **Indoor** com `production-service`, `gateway-capturer`, e `capture-service`.
3. Os dados processados são então enviados para a parte **Outdoor**, onde o `gateway-processor` e o `processor-service` realizam o processamento mais pesado, coletando métricas de desempenho e armazenando os dados processados.
4. O **core-service** coleta os dados processados e os armazena em um banco de dados histórico.
5. O **metrics-dashboard** fornece a interface para visualizar as métricas de desempenho em tempo real.

---

# Tecnologias Utilizadas

- **Frontend (Prediction-Frontend, Metrics-Dashboard):** React.js
- **Backend (Spring Boot):** Java, Spring Boot, RabbitMQ
- **Modelos de Previsão:** Python, Flask, ARIMA, Stochastic Tuning
- **Processamento de Dados:** C (para agregação, compressão e filtragem)
- **Mensageria:** RabbitMQ
- **Banco de Dados:** PostgreSQL

---

### Imagens de Arquitetura

A seguir estão as imagens representando a arquitetura do sistema. A **Figura 3** mostra a arquitetura utilizando o padrão TAM da SAP, enquanto a **Figura 4** descreve a arquitetura detalhada de captura e processamento de dados.

**Figura 3 - Arquitetura do Sistema (Padrão TAM da SAP)**
![Figura 3 - Arquitetura do Sistema](file-TjnkY4re2GAy9RQ6DFzY3X.png)

**Figura 4 - Arquitetura de Processamento de Dados de Sensores**
![Figura 4 - Arquitetura de Processamento de Dados](file-1xgrXUFZygUzfKp9qdm6fJ.png)

---

Isso fornece uma explicação clara e detalhada sobre a arquitetura do sistema, como cada módulo funciona e quais tecnologias são utilizadas. A marcação das imagens foi realizada para que você possa facilmente adicioná-las ao seu arquivo Markdown.
