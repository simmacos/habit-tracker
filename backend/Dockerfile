FROM maven:3.9-amazoncorretto-21 AS builder
WORKDIR /app
# Clona il repository e vai nella cartella backend
RUN git clone https://github.com/simmacos/habit-tracker.git .
WORKDIR /app/backend
# Build con Maven
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app
# Copia il JAR dalla fase di build
COPY --from=builder /app/backend/target/*.jar app.jar
# Entrypoint per eseguire l'applicazione
ENTRYPOINT ["java", "-jar", "/app/app.jar"]