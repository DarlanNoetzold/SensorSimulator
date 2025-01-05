@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

:: Definindo os IPs fixos para RabbitMQ e PostgreSQL
set RABBITMQ_IP=172.17.0.4
set POSTGRES_IP=172.17.0.3

:: Parar e remover contêineres antigos
echo Parando e removendo contêineres antigos...
docker stop rabbitmq config_service_db
docker rm rabbitmq config_service_db

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

:: Aguardar o PostgreSQL estar pronto
echo Esperando o PostgreSQL estar pronto...
timeout /t 10 /nobreak

:: Criar os bancos de dados necessários no PostgreSQL
echo Criando bancos de dados no PostgreSQL...
docker exec -it config_service_db psql -U postgres -c "CREATE DATABASE config_service;"
docker exec -it config_service_db psql -U postgres -c "CREATE DATABASE predictions;"
docker exec -it config_service_db psql -U postgres -c "CREATE DATABASE predictions_processor;"
docker exec -it config_service_db psql -U postgres -c "CREATE DATABASE sensorFinalData;"

:: Verificar se os bancos foram criados
echo Verificando os bancos criados...
docker exec -it config_service_db psql -U postgres -c "\l"

:: Aguardar os contêineres estarem prontos
echo Esperando 5 segundos, pressione CTRL+C para sair...
timeout /t 5 /nobreak

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
pip install -r ./requirements.txt
start python app.py
cd ..

echo Iniciando o serviço Spring - config-service...
cd config-service
start mvn spring-boot:run -DskipTests
cd ..

:: Iniciar os serviços Spring (data-handler, core-service, gateway-capturer, gateway-processor, production-service)
echo Iniciando o serviço Spring - data-handler...
cd data-handler
start mvn spring-boot:run -DskipTests -Dspring.datasource.url=jdbc:postgresql://%POSTGRES_IP%:5432/config_service
cd ..

echo Iniciando o serviço Spring - core-service...
cd core-service
start mvn spring-boot:run -DskipTests -Dspring.datasource.url=jdbc:postgresql://%POSTGRES_IP%:5432/core_service
cd ..

echo Iniciando o serviço Spring - gateway-capturer...
cd gateway-capturer
start mvn spring-boot:run -DskipTests -Dspring.datasource.url=jdbc:postgresql://%POSTGRES_IP%:5432/gateway_capturer
cd ..

echo Iniciando o serviço Spring - gateway-processor...
cd gateway-processor
start mvn spring-boot:run
cd ..

echo Iniciando o serviço Spring - production-service...
cd production-service
start mvn spring-boot:run -DskipTests -Dspring.datasource.url=jdbc:postgresql://%POSTGRES_IP%:5432/production_service
cd ..

echo Compilando Docker Image do Capture-service...
cd Capture-service
docker build -t tech/noetzold/capture-service:latest .
cd ..

echo Compilando Docker Image do processor-service...
cd processor-service
docker build -t tech/noetzold/processor-service:latest .
cd ..


:: Verificar se todos os serviços estão funcionando
echo Todos os serviços estão em funcionamento.
pause
