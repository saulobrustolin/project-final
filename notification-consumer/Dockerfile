FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY shared/ ./shared/
RUN mvn clean install -f shared/pom.xml -DskipTests

COPY notification-consumer/ ./notification-consumer/
RUN mvn clean package -f notification-consumer/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd -m spring
USER spring

COPY --from=build /app/notification-consumer/target/notification-consumer-4.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]