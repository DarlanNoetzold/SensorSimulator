#!/bin/bash

# Definindo os IPs fixos para RabbitMQ e PostgreSQL
RABBITMQ_IP=172.17.0.2
POSTGRES_IP=172.17.0.3

# Parar e remover contêineres antigos
echo "Parando e removendo contêineres antigos..."
docker stop rabbitmq config_service_db
docker rm rabbitmq config_service_db

# Construir as imagens do RabbitMQ e PostgreSQL
echo "Construindo imagens do RabbitMQ e PostgreSQL..."
docker build -t custom-rabbitmq ./rabbitmq
docker build -t custom-postgres ./postgres

# Rodar o contêiner do RabbitMQ
echo "Subindo o contêiner do RabbitMQ..."
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 custom-rabbitmq

# Rodar o contêiner do PostgreSQL
echo "Subindo o contêiner do PostgreSQL..."
docker run -d --name config_service_db -p 5432:5432 custom-postgres

# Aguardar o PostgreSQL estar pronto
echo "Esperando o PostgreSQL estar pronto..."
sleep 10

# Criar os bancos de dados necessários no PostgreSQL
echo "Criando bancos de dados no PostgreSQL..."
docker exec -it config_service_db psql -U postgres -c "CREATE DATABASE config_service;"
docker exec -it config_service_db psql -U postgres -c "CREATE DATABASE predictions;"
docker exec -it config_service_db psql -U postgres -c "CREATE DATABASE predictions_processor;"
docker exec -it config_service_db psql -U postgres -c "CREATE DATABASE sensorFinalData;"
docker exec -it config_service_db psql -U postgres -c "CREATE DATABASE metrics_db;"

# Verificar se os bancos foram criados
echo "Verificando os bancos criados..."
docker exec -it config_service_db psql -U postgres -c "\l"

# Aguardar os contêineres estarem prontos
echo "Esperando 5 segundos, pressione CTRL+C para sair..."
sleep 5

# Verificar se os contêineres estão rodando
echo "Verificando contêineres em execução..."
docker ps

# Iniciar serviços React (prediction-frontend, metrics-dashboard)
echo "Iniciando o serviço React - prediction-frontend..."
(cd prediction-frontend && npm start &) 

wait_seconds 5

echo "Iniciando o serviço React - metrics-dashboard..."
(cd metrics-dashboard && npm start &) 

wait_seconds 5

# Iniciar o serviço Python (prediction-service)
echo "Iniciando o serviço Python - prediction-service..."
(cd prediction-service && pip install -r ./requirements.txt && python app.py &) 

wait_seconds 5

# Iniciar serviços Spring Boot com Maven
start_spring_service() {
  local service_dir=$1
  local db_name=$2
  (cd $service_dir && mvn spring-boot:run -DskipTests -Dspring.datasource.url=jdbc:postgresql://$POSTGRES_IP:5432/$db_name &) 
}

echo "Iniciando o serviço Spring - config-service..."
start_spring_service "config-service" "config_service"
wait_seconds 5

echo "Iniciando o serviço Spring - data-handler..."
start_spring_service "data-handler" "config_service"
wait_seconds 5

echo "Iniciando o serviço Spring - core-service..."
start_spring_service "core-service" "core_service"
wait_seconds 5

echo "Iniciando o serviço Spring - gateway-capturer..."
start_spring_service "gateway-capturer" "gateway_capturer"
wait_seconds 5

echo "Iniciando o serviço Spring - gateway-processor..."
(cd gateway-processor && mvn spring-boot:run &)
wait_seconds 5

echo "Iniciando o serviço Spring - production-service..."
start_spring_service "production-service" "production_service"
wait_seconds 5

# Compilando Docker Images dos serviços Capture e Processor
echo "Compilando Docker Image do Capture-service..."
(cd Capture-service && docker build -t tech/noetzold/capture-service:latest .)

echo "Compilando Docker Image do processor-service..."
(cd processor-service && docker build -t tech/noetzold/processor-service:latest .)

# Mantém o container rodando indefinidamente
echo "Todos os serviços estão em funcionamento."
tail -f /dev/null
