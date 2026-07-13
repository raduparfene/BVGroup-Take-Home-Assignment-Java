# BVGroup Take-Home Assignment - Java
This project will contain the two applications required by the assignment.

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
- Words are compared case-insensitively, punctuation is treated as a separator, and ties are resolved alphabetically
- Processing times are measured in milliseconds using `System.nanoTime()`
- Paragraph processing time covers only local text analysis. Total processing time includes the Hipsum calls and analysis, but excludes Kafka publication
- Invalid or missing `p` returns HTTP `400` without calling Hipsum or publishing a message
- `freq_word` is used as the Kafka record key so equal words reach the same partition
- A successful response is returned only after Kafka acknowledges the message; a failed Hipsum call fails the whole request rather than producing a partial result
- `/betvictor/history` returns a JSON array ordered newest first by database persistence order
