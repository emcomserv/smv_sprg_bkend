# Stage 1: Build the application using Maven and Java 21
FROM maven:3.9.10-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

RUN cp target/*.jar app.jar

# Stage 2: Use secure, minimal distroless runtime with Java 21
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/app.jar /app/app.jar

EXPOSE 7082

# Correct CMD syntax for distroless image
CMD ["java", "-jar", "app.jar"]
