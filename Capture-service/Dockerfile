# Usando a imagem base do OpenJDK (ou qualquer imagem que atenda a sua aplicação)
FROM openjdk:17-jdk-slim

# Diretório de trabalho para a aplicação
WORKDIR /app

# Copiar os arquivos JAR da sua aplicação para o contêiner
COPY target/Capture-service-0.0.1-SNAPSHOT.jar /app/Capture-service-0.0.1-SNAPSHOT.jar

# Expor a porta que o Capture-Service vai utilizar
EXPOSE 8081

# Comando para rodar a aplicação no contêiner
ENTRYPOINT ["java", "-jar", "Capture-service-0.0.1-SNAPSHOT.jar"]

#docker build -t tech/noetzold/capture-service:latest .