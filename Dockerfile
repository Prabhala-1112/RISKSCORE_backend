
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Build frontend inside Docker (optional, but ensures consistent environment)
# For simplicity in this Dockerfile, we assume the user ran 'prepare_for_deploy.ps1' locally
# or we can do a multi-stage build. Let's do a pure Java build for simplicity + reliability.
# Ensure mvnw is executable
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java","-jar","app.jar"]
