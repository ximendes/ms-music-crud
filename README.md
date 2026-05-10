# CRUD Music API

Spring Boot REST API for managing music records in a DynamoDB table.

The application exposes CRUD endpoints under `/music` and stores each item in the `Music` DynamoDB table. Each record contains an artist, song title, and release year, using:

- `Artist` as the partition key
- `SongTitle` as the sort key

For local development, the project uses LocalStack to emulate AWS DynamoDB.

## Technologies and Versions

| Technology | Version | Purpose |
| --- | --- | --- |
| Java | 21 | Application runtime and compilation target |
| Gradle Wrapper | 9.4.0 | Build tool |
| Spring Boot | 4.0.4 | Web application framework |
| Spring Dependency Management Plugin | 1.1.7 | Dependency version management |
| AWS SDK for Java BOM | 2.42.33 | AWS SDK dependency alignment |
| AWS SDK DynamoDB | 2.42.33 | Low-level DynamoDB client |
| AWS SDK DynamoDB Enhanced Client | 2.42.33 | Object mapping for DynamoDB records |
| LocalStack | latest Docker image | Local AWS service emulator |

## Requirements

- Java 21
- Docker and Docker Compose
- Bash-compatible shell

The Gradle wrapper is included, so a local Gradle installation is not required.

## Local Configuration

The application reads DynamoDB settings from `src/main/resources/application.yaml`.

Default local values:

```yaml
aws:
  dynamodb:
    access-key: teste
    secret-key: teste
    region: us-east-1
    endpoint: ${AWS_DYNAMODB_ENDPOINT:http://localhost:4566}
```

When running the application directly from your machine, it connects to LocalStack at:

```text
http://localhost:4566
```

When running the application in Docker, use:

```text
http://localstack:4566
```

## Start Locally

### 1. Start LocalStack and create the DynamoDB table

From the project root:

```bash
docker compose -f Docker/docker-compose.yaml up -d
```

The compose file mounts `Docker/create-dynamo.sh` into LocalStack. When LocalStack is ready, the script creates the `Music` table if it does not already exist.

### 2. Run the application

```bash
./gradlew bootRun
```

The API will start on:

```text
http://localhost:8080
```

### 3. Run tests

```bash
./gradlew test
```

## API Usage

Request and response bodies use this shape:

```json
{
  "artist": "Daft Punk",
  "songTitle": "One More Time",
  "year": 2000
}
```

### Create a music record

```bash
curl -X POST http://localhost:8080/music \
  -H "Content-Type: application/json" \
  -d '{
    "artist": "Daft Punk",
    "songTitle": "One More Time",
    "year": 2000
  }'
```

### Get a music record

```bash
curl http://localhost:8080/music/Daft%20Punk/One%20More%20Time
```

### Update a music record

```bash
curl -X PUT http://localhost:8080/music \
  -H "Content-Type: application/json" \
  -d '{
    "artist": "Daft Punk",
    "songTitle": "One More Time",
    "year": 2000
  }'
```

### Delete a music record

```bash
curl -X DELETE http://localhost:8080/music/Daft%20Punk/One%20More%20Time
```

## Build

Build the application JAR:

```bash
./gradlew clean build
```

The executable JAR is generated in:

```text
build/libs/
```

## Docker

### Build the image manually

```bash
docker build -t my-app:v1.0 -f Docker/Dockerfile .
```

### Run the image manually

Make sure LocalStack is already running:

```bash
docker compose -f Docker/docker-compose.yaml up -d
```

Then run the application container on the same Docker network as LocalStack:

```bash
docker run -d -p 8080:8080 \
  --name crud-container \
  --network docker_default \
  -e AWS_DYNAMODB_ENDPOINT=http://localstack:4566 \
  my-app:v1.0
```

### Build and run using the helper script

```bash
./build-image.sh
```

By default, the script builds the image as:

```text
my-app:v1.0
```

You can also pass a custom image name and tag:

```bash
./build-image.sh my-app v2.0
```
