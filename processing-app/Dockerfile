FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY processing-app processing-app
RUN mvn -f processing-app/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run as non-root so that a compromised application has fewer permissions
RUN addgroup --system --gid 10001 app \
    && adduser --system --uid 10001 --ingroup app app

COPY --from=build /workspace/processing-app/target/processing-app-*.jar application.jar

EXPOSE 8081

USER app

HEALTHCHECK --interval=10s --timeout=5s --start-period=10s --retries=5 \
    CMD ["wget", "-q", "-O", "/dev/null", "http://localhost:8081/actuator/health"]

ENTRYPOINT ["java", "-jar", "application.jar"]
