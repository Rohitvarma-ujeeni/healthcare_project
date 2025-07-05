# Stage 1: Build the app with Maven
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
EXPOSE 8080

# Stage 2: Run the app with a lightweight JDK image
FROM openjdk:17-jdk-slim
WORKDIR /healthcare_project
COPY --from=build /app/target/*.jar healthcare_project.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "healthcare_project.jar"]
