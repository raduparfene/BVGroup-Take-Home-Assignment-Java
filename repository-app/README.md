# Repository Application
This application consumes processed text results from Kafka, stores them in an H2 database and exposes the latest results through an HTTP endpoint.

## Requirements
- Java 21
- H2 Database
- Apache Kafka

## Running the application
From the repository root, run:

Windows:
```powershell
.\mvnw.cmd -pl repository-app spring-boot:run
```
Linux/macOS:
```bash
./mvnw -pl repository-app spring-boot:run
```
The application starts on port `8082` by default. H2 stores its data locally in the `.local-data` directory, so only Kafka must be started separately

## Kafka consumption
- The application consumes the four field JSON produced by `processing-app` from topic `words.processed` by default. Messages are consumed as strings and deserialized before being stored
- The topic partitions and consumer concurrency are different concepts. The topic has four partitions, while concurrency controls how many consumer threads run in this application instance 
- With the default concurrency of `4`, Kafka can assign one partition to each consumer thread
- `earliest` makes a new consumer group start from the oldest available message when it has no saved offset
- Record acknowledgment saves the offset after each message was processed successfully

## API
`GET /betvictor/history`
The endpoint returns the latest 10 persisted computation results, ordered newest first by database-generated ID

Example response:
```json
[
  {
    "freq_word": "artisan",
    "avg_paragraph_size": 45.0,
    "avg_paragraph_processing_time": 0.3,
    "total_processing_time": 210.5
  }
]
```

## Configuration
The application can be configured through environment variables:

| Environment variable          | Default value                                      | Description                                      |
|-------------------------------|----------------------------------------------------|--------------------------------------------------|
| `SERVER_PORT`                 | `8082`                                             | HTTP server port                                 |
| `HISTORY_LIMIT`               | `10`                                               | Number of recent results returned by the API     |
| `KAFKA_TOPIC_NAME`            | `words.processed`                                  | Kafka topic containing processed results         |
| `KAFKA_CONSUMER_CONCURRENCY`  | `4`                                                | Number of concurrent consumers in this instance  |
| `KAFKA_BOOTSTRAP_SERVERS`     | `localhost:9092`                                   | Kafka broker addresses                           |
| `KAFKA_CONSUMER_GROUP_ID`     | `repository-app`                                   | Kafka consumer group                             |
| `KAFKA_AUTO_OFFSET_RESET`     | `earliest`                                         | Initial offset policy for a new consumer group   |
| `DATABASE_URL`                | `jdbc:h2:file:./.local-data/betvictor`             | H2 database location                             |
| `JPA_DDL_AUTO`                | `update`                                           | Hibernate schema management mode                 |
| `KAFKA_CLIENT_LOG_LEVEL`      | `WARN`                                             | Logging level for the Apache Kafka client        |

## Delivery semantics
- Each message is saved before Kafka marks it as consumed 
- If processing fails, Kafka can send the message again
- This also means that duplicate rows are possible after certain failures

## Running the tests
From the repository root, run:

Windows:
```powershell
.\mvnw.cmd -pl repository-app test
```
Linux/macOS:
```bash
./mvnw -pl repository-app test
```

The tests use an in memory H2 database and do not connect to a real Kafka broker. They verify message deserialization and persistence, together with the latest 10 HTTP contract.
