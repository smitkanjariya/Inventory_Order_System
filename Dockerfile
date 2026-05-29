FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

RUN mvn clean package

FROM payara/server-full:latest

COPY --from=build /app/target/*.war $DEPLOY_DIR

EXPOSE 8080