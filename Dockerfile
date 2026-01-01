
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Build frontend inside Docker (optional, but ensures consistent environment)
# For simplicity in this Dockerfile, we assume the user ran 'prepare_for_deploy.ps1' locally
# or we can do a multi-stage build. Let's do a pure Java build for simplicity + reliability.
# Avoid using ./mvnw to prevent Windows line-ending (CRLF) issues
# The base image already has Maven installed.
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/risk_dataset.csv risk_dataset.csv
EXPOSE 8091
ENTRYPOINT ["java","-jar","app.jar"]
