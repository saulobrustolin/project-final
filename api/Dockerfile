FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY shared/ ./shared/
RUN mvn clean install -f shared/pom.xml -DskipTests

COPY api/ ./api/
RUN mvn clean package -f api/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd -m spring
USER spring

COPY --from=build /app/api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]