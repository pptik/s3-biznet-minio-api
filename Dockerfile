FROM maven:3.9.10-eclipse-temurin-21 AS build

WORKDIR /app

LABEL version="0.0.1-SNAPSHOT"

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/spring-0.0.1-SNAPSHOT.jar ./spring-0.0.1-SNAPSHOT.jar

CMD [ "java", "-jar", "./spring-0.0.1-SNAPSHOT.jar" ]

EXPOSE 8080