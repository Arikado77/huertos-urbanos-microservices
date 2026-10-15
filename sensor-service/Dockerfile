FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/sensor-service-0.0.1-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 8082
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 CMD wget -q -O /dev/null http://127.0.0.1:8082/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
