FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY repository-app repository-app
RUN mvn -f repository-app/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run as non-root so that a compromised application has fewer permissions
# Give that user write access to the H2 data directory
RUN addgroup --system --gid 10001 app \
    && adduser --system --uid 10001 --ingroup app app \
    && mkdir -p /app/.local-data \
    && chown app:app /app/.local-data

COPY --from=build /workspace/repository-app/target/repository-app-*.jar application.jar

EXPOSE 8082

USER app

HEALTHCHECK --interval=10s --timeout=5s --start-period=10s --retries=5 \
    CMD ["wget", "-q", "-O", "/dev/null", "http://localhost:8082/actuator/health"]

ENTRYPOINT ["java", "-jar", "application.jar"]
