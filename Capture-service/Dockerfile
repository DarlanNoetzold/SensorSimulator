# Usando a imagem base do OpenJDK (ou qualquer imagem que atenda a sua aplicação)
FROM openjdk:17-jdk-slim

# Diretório de trabalho para a aplicação
WORKDIR /app

# Copiar os arquivos JAR da sua aplicação para o contêiner
COPY target/capture-service.jar /app/capture-service.jar

# Expor a porta que o Capture-Service vai utilizar
EXPOSE 8081

# Comando para rodar a aplicação no contêiner
ENTRYPOINT ["java", "-jar", "capture-service.jar"]