# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .

# Pre-download dependencies for Docker layer caching
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests


# Run stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/voting-system-0.0.1-SNAPSHOT.jar app.jar

# Render provides the PORT environment variable
EXPOSE 8080

# JVM memory optimization for Render Free
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]