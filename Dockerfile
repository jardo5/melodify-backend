FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/Melodify-0.0.1-SNAPSHOT.jar app.jar
COPY .env /app/.env
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
