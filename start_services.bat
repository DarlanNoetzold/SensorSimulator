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

echo Todos os serviços estão em funcionamento.
pause
