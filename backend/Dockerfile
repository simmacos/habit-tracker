FROM maven:3.9-amazoncorretto-21 AS builder
WORKDIR /app
# Copia i file del progetto backend
COPY . .
# Build con Maven
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app
# Copia il JAR dalla fase di build
COPY --from=builder /app/target/*.jar app.jar
# Entrypoint per eseguire l'applicazione
ENTRYPOINT ["java", "-jar", "/app/app.jar"]