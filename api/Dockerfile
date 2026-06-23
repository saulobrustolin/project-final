FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY shared/ ./shared/
RUN mvn clean install -f shared/pom.xml -DskipTests

COPY api/ ./api/
RUN mvn clean package -f api/pom.xml -DskipTests

COPY notification-consumer/ ./notification-consumer/
RUN mvn clean package -f notification-consumer/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd -m spring

COPY entrypoint.sh .
RUN chmod +x entrypoint.sh && chown spring:spring entrypoint.sh

COPY --from=build /app/api/target/*.jar api.jar
COPY --from=build /app/notification-consumer/target/*.jar consumer.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["./entrypoint.sh"]