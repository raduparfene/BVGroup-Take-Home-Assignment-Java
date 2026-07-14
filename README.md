# BVGroup Take-Home Assignment - Java
This project contains the two applications required by the assignment.

## Architecture
Client > Processing App > Hipsum API > Kafka > Repository App > Database

## Tech Stack
- Java 21
- Spring Boot 4.1
- Lombok
- Apache Kafka
- H2 Database
- Docker Compose
- JUnit 6 / Mockito / REST Assured

## Running the complete system
Docker Compose builds and starts Kafka and both applications.
Kafka runs in KRaft mode, without ZooKeeper, using the official `apache/kafka:4.2.1` image.
Kafka configuration and topic creation are kept under `docker/kafka`. Docker environment values are kept under `docker/environment`.
The application containers run as a non-root user.

From the repository root, run the complete system with:

Windows:
```powershell
.\scripts\start.bat full
```
Linux/macOS:
```bash
sh scripts/start.sh full
```
The `full` mode builds and starts the complete system. Docker recreates containers when needed.
The stack exposes:
- Processing API: `http://localhost:8081/betvictor/text?p=3`
- Repository API: `http://localhost:8082/betvictor/history`
- Processing health: `http://localhost:8081/actuator/health`
- Repository health: `http://localhost:8082/actuator/health`
- Kafka broker for applications running on the host: `localhost:9092`

The setup creates topic `words.processed` with four partitions. The topic name and partition count can be changed through `KAFKA_TOPIC_NAME` and `KAFKA_TOPIC_PARTITIONS`.
The `kafka-topic-setup` container creates the topic and then stops, so `Exited (0)` means that it completed successfully.
Applications inside Docker use `kafka:9092`, while applications started from the IDE use `localhost:9092`. Port `29092` is only used by Docker to expose Kafka on `localhost:9092`.
`KAFKA_BOOTSTRAP_SERVERS` can point both applications to a different Kafka broker.

Docker volumes keep the Kafka and H2 data when containers are recreated. Stop the containers without deleting their data with:
```powershell
docker compose down
```
Use `docker compose down -v` only when the local Kafka and database data should also be removed.

## Local development and debugging
For local debugging, run Kafka in Docker and start both Spring Boot applications from the IDE.

Windows:
```powershell
.\scripts\start.bat local
```
Linux/macOS:
```bash
sh scripts/start.sh local
```
The `local` mode stops the application containers, starts Kafka and creates the topic. `ProcessingApplication` and `RepositoryApplication` can then be started from the IDE using Java 21. They connect to Kafka through `localhost:9092`, so no remote debugging setup is required.

## Verification
Run all automated tests from the repository root:
```powershell
.\mvnw.cmd clean verify
```

## Requirements
The checklists below track implementation and verification status

### Processing application
- [x] Java server application exposing `GET /betvictor/text`
- [x] Accept a required query parameter named `p` (`p > 0`)
- [x] For `p = N`, make exactly `N` requests to
  `https://hipsum.co/api/?type=hipster-centric&paras=1`
- [x] Request only one paragraph per Hipsum call
- [x] Process all returned paragraphs as one computation and calculate:
  - the most frequent word;
  - the average paragraph size;
  - the average time spent analyzing a paragraph;
  - the total processing time
- [x] Return the result as JSON using the fields `freq_word`, `avg_paragraph_size`, `avg_paragraph_processing_time`, and `total_processing_time`
- [x] Publish exactly one Kafka message to topic `words.processed` for every successful request
- [x] Use the same four-field payload in both the HTTP response and the Kafka message
- [x] Preserve the send order of messages having the same `freq_word`
- [x] Design the Kafka producer assuming topic `words.processed` has four partitions
- [x] Allow the producer Kafka broker address to be supplied through external configuration

### Repository application
- [x] Consume messages from Kafka topic `words.processed`
- [x] Persist consumed results in a datasource
- [x] Expose `GET /betvictor/history`
- [x] Return the latest 10 computation results from the datasource
- [x] Make the number of concurrent Kafka consumers configurable
- [x] Run concurrent consumers as separate threads in one application instance
- [x] Allow the consumer Kafka broker address to be supplied through external configuration

## Implementation decisions
- [x] Independent Hipsum calls are executed concurrently to reduce total request latency
- [x] Concurrency is bounded/configurable to avoid overwhelming the external API

## Assumptions
The brief leaves a few details unspecified. Unless clarified otherwise, this project will use the following conventions:

- `avg_paragraph_size` is the average number of normalized words per paragraph
- Words are compared case-insensitively, contain letters only, punctuation is treated as a separator, and ties are resolved alphabetically
- Processing times are measured in milliseconds using `System.nanoTime()`
- Paragraph processing time covers only local text analysis. Total processing time includes the Hipsum calls and analysis, but excludes Kafka publication
- Invalid or missing `p` returns HTTP `400` without calling Hipsum or publishing a message
- `freq_word` is used as the Kafka record key so equal words reach the same partition
- A successful response is returned only after Kafka acknowledges the message; a failed Hipsum call fails the whole request rather than producing a partial result
- `/betvictor/history` returns a JSON array ordered newest first by database persistence order
