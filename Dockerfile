# Construção da aplicação
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Criação da imagem de execução leve
# Imagem base oficial do OpenJDK com a versão 17
FROM openjdk:17-jdk-slim
# Diretório de trabalho dentro do container
WORKDIR /app
# Copiar o JAR para o container
COPY --from=build /app/target/hackathon-backend-0.0.1-SNAPSHOT.jar hackathon.jar
# Configuração de fuso horário
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# Expor a porta 8080 para que a aplicação seja acessível de fora do container
EXPOSE 8080
# Rodar a aplicação quando o container iniciar
ENTRYPOINT ["java","-jar","/app/hackathon.jar"]
