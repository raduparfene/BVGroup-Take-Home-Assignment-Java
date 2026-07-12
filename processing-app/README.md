# Processing Application
This application retrieves paragraphs from Hipsum and returns aggregated text statistics through an HTTP endpoint.
Kafka publishing is not implemented in this module yet.

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
All time values are expressed in milliseconds. Invalid or missing `p` returns HTTP `400`. A failed Hipsum request or a response rejected by the Hipsum client returns HTTP `502`

## Configuration
The application can be configured through environment variables:

| Environment variable | Default value | Description |
| --- | --- | --- |
| `SERVER_PORT` | `8081` | HTTP server port |
| `HIPSUM_BASE_URL` | `https://hipsum.co` | Hipsum base URL |
| `HIPSUM_TYPE` | `hipster-centric` | Hipsum text type |
| `HIPSUM_MAX_CONCURRENCY` | `8` | Maximum concurrent Hipsum requests |

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

The test suite covers the text analysis rules, Hipsum request and response handling, service orchestration, HTTP contract, validation and external API failures. The tests do not call the real Hipsum service.
