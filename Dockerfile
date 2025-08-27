# Build da aplicação
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -DskipITs

# Criação da imagem de execução "leve"
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/hackathon-backend-0.0.1-SNAPSHOT.jar hackathon.jar
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/hackathon.jar"]
