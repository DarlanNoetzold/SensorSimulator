@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

:: Parar e remover contêineres antigos
echo Parando e removendo contêineres antigos...
docker stop rabbitmq config_service_db capture-service
docker rm rabbitmq config_service_db capture-service

:: Construir as imagens do RabbitMQ e PostgreSQL
echo Construindo imagens do RabbitMQ e PostgreSQL...
docker build -t custom-rabbitmq .\rabbitmq
docker build -t custom-postgres .\postgres

:: Rodar o contêiner do RabbitMQ
echo Subindo o contêiner do RabbitMQ...
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 custom-rabbitmq

:: Rodar o contêiner do PostgreSQL
echo Subindo o contêiner do PostgreSQL...
docker run -d --name config_service_db -p 5432:5432 custom-postgres

:: Aguardar os contêineres estarem prontos
echo Esperando os contêineres estarem prontos...
timeout /t 10 /nobreak

:: Verificar se os contêineres estão rodando
echo Verificando contêineres em execução...
docker ps

:: Iniciar serviços React (prediction-frontend, metrics-dashboard)
echo Iniciando o serviço React - prediction-frontend...
cd prediction-frontend
start npm start
cd ..

echo Iniciando o serviço React - metrics-dashboard...
cd metrics-dashboard
start npm start
cd ..

:: Iniciar o serviço Python (prediction-service)
echo Iniciando o serviço Python - prediction-service...
cd prediction-service
start python app.py
cd ..

:: Iniciar os serviços Spring (data-handler, core-service, gateway-capturer, gateway-processor, production-service)
echo Iniciando o serviço Spring - data-handler...
cd data-handler
start mvn spring-boot:run -DskipTests
cd ..

echo Iniciando o serviço Spring - core-service...
cd core-service
start mvn spring-boot:run -DskipTests
cd ..

echo Iniciando o serviço Spring - gateway-capturer...
cd gateway-capturer
start mvn spring-boot:run -DskipTests
cd ..

echo Iniciando o serviço Spring - gateway-processor...
cd gateway-processor
start mvn spring-boot:run -DskipTests
cd ..

echo Iniciando o serviço Spring - production-service...
cd production-service
start mvn spring-boot:run -DskipTests
cd ..

:: Verificar se todos os serviços estão funcionando
echo Todos os serviços estão em funcionamento.
pause
