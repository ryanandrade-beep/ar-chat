# Etapa 1: compila e empacota o jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Etapa 2: imagem final só com o JRE
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system archat
COPY --from=build /app/target/ar-chat-backend-*.jar app.jar
USER archat
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
