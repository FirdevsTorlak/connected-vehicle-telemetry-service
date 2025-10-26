# Multi-stage Docker build: build JAR, then run it
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -ntp -q dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests=true package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/telemetry-service-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
