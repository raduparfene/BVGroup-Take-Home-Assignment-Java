# Processing Application
This application retrieves paragraphs from Hipsum, returns aggregated text statistics through an HTTP endpoint and publishes the same result to Kafka.

## Requirements
- Java 21

## Running the application
From the repository root, run:

Windows:
```powershell
.\mvnw.cmd -pl processing-app spring-boot:run
```
Linux/macOS:
```bash
./mvnw -pl processing-app spring-boot:run
```
The application starts on port `8081` by default

## API
`GET /betvictor/text?p={paragraphCount}`
`p` is required and must be greater than zero. For `p = N`, the application sends exactly `N` Hipsum requests, each requesting one paragraph with `paras=1`

Example request:
```text
http://localhost:8081/betvictor/text?p=3
```
Example response:
```json
{
  "freq_word": "artisan",
  "avg_paragraph_size": 45.0,
  "avg_paragraph_processing_time": 0.3,
  "total_processing_time": 210.5
}
```
All time values are expressed in milliseconds. Invalid or missing `p` returns HTTP `400`. A failed Hipsum request or a response rejected by the Hipsum client returns HTTP `502`. A Kafka publishing failure returns HTTP `503`

## Kafka publishing
Every successful request publishes one message to topic `words.processed` by default. The Kafka value contains the same four-field JSON returned by the HTTP endpoint
`freq_word` is used as the Kafka record key. Equal keys are assigned to the same partition, where Kafka preserves their order. The producer waits for the broker acknowledgment before returning HTTP `200`
The topic is expected to exist with four partitions

## Configuration
The application can be configured through environment variables:

| Environment variable             | Default value       | Description                                                                  |
|----------------------------------|---------------------|------------------------------------------------------------------------------|
| `SERVER_PORT`                    | `8081`              | HTTP server port                                                             |
| `HIPSUM_BASE_URL`                | `https://hipsum.co` | Hipsum base URL                                                              |
| `HIPSUM_TYPE`                    | `hipster-centric`   | Hipsum text type                                                             |
| `HIPSUM_MAX_CONCURRENCY`         | `8`                 | Maximum concurrent Hipsum requests                                           |
| `KAFKA_TOPIC_NAME`               | `words.processed`   | Kafka topic receiving the processing results                                 |
| `KAFKA_BOOTSTRAP_SERVERS`        | `localhost:9092`    | Kafka broker addresses                                                       |
| `KAFKA_MAX_BLOCK_MS`             | `5000`              | Maximum time a Kafka send can wait for metadata or buffer space              |
| `KAFKA_REQUEST_TIMEOUT_MS`       | `10000`             | Maximum time to wait for a broker response                                   |
| `KAFKA_DELIVERY_TIMEOUT_MS`      | `15000`             | Maximum total time allowed for delivering a Kafka message, including retries |
| `KAFKA_RECONNECT_BACKOFF_MS`     | `1000`              | Initial delay between Kafka broker reconnection attempts                     |
| `KAFKA_RECONNECT_BACKOFF_MAX_MS` | `10000`             | Maximum delay between Kafka broker reconnection attempts                     |
| `KAFKA_CLIENT_LOG_LEVEL`         | `WARN`              | Logging level for the Apache Kafka client                                    |

The Hipsum requests are executed concurrently using a fixed-size thread pool

## Assumptions
- Paragraph size means the number of normalized words in a paragraph
- Words are compared case-insensitively
- Punctuation is treated as a word separator
- Frequency ties are resolved alphabetically
- Average paragraph processing time covers local text analysis only
- Total processing time includes the Hipsum requests and local analysis

## Running the tests
From the repository root, run:

Windows:
```powershell
.\mvnw.cmd -pl processing-app test
```
Linux/macOS:
```bash
./mvnw -pl processing-app test
```

The test suite covers the text analysis rules, Hipsum request and response handling, service orchestration, HTTP contract, Kafka publishing, validation and external system failures. The tests do not call the real Hipsum service or a real Kafka broker.
