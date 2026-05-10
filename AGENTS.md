# AGENTS.md

Guidelines for AI coding agents working in this repository.

## Project Overview

This is a Java 21 Spring Boot CRUD API for managing music records in DynamoDB.

The API exposes endpoints under `/music` and stores records in the `Music` DynamoDB table. Each record contains an artist, song title, and release year.

Primary DynamoDB keys:

- Partition key: `Artist`
- Sort key: `SongTitle`

Local development uses LocalStack to emulate DynamoDB.

## Project Structure

```text
.
├── build.gradle
├── gradle/libs.versions.toml
├── settings.gradle
├── gradlew
├── gradlew.bat
├── README.md
├── AGENTS.md
├── build-image.sh
├── Docker/
│   ├── Dockerfile
│   ├── docker-compose.yaml
│   └── create-dynamo.sh
└── src/
    ├── main/
    │   ├── java/com/example/crud/
    │   │   ├── CrudApplication.java
    │   │   ├── config/
    │   │   │   ├── DynamoDbProperties.java
    │   │   │   ├── aws/DynamoDBConfig.java
    │   │   │   └── persistence/DynamoDbEnhancedConfig.java
    │   │   ├── entrypoint/
    │   │   │   ├── MusicController.java
    │   │   │   ├── MusicRequest.java
    │   │   │   └── MusicResponse.java
    │   │   ├── repository/
    │   │   │   ├── Music.java
    │   │   │   └── MusicRepository.java
    │   │   └── service/
    │   │       ├── MusicDTO.java
    │   │       └── MusicService.java
    │   └── resources/
    │       └── application.yaml
    └── test/
        └── java/com/example/crud/
            └── CrudApplicationTests.java
```

## Main Layers

- `entrypoint`: HTTP controllers and request/response records.
- `service`: Application use cases and DTOs.
- `repository`: DynamoDB entity mapping and persistence operations.
- `config`: AWS SDK and DynamoDB Enhanced Client configuration.
- `Docker`: LocalStack, DynamoDB table initialization, and application Docker image files.

## Dependency Versions

Dependency versions are managed in `gradle/libs.versions.toml`.

Current major versions:

- Java: `21`
- Gradle wrapper: `9.4.0`
- Spring Boot: `4.0.4`
- Spring dependency management plugin: `1.1.7`
- AWS SDK for Java: `2.42.33`

When changing dependency versions, update `gradle/libs.versions.toml` and verify with:

```bash
./gradlew clean build
```

## Local Development Commands

Start LocalStack and create the DynamoDB table:

```bash
docker compose -f Docker/docker-compose.yaml up -d
```

Run the application locally:

```bash
./gradlew bootRun
```

Run tests:

```bash
./gradlew test
```

Build the project:

```bash
./gradlew clean build
```

Build and run Docker image using the helper script:

```bash
./build-image.sh
```

## Local Runtime Configuration

The application reads configuration from `src/main/resources/application.yaml`.

Default DynamoDB endpoint:

```text
http://localhost:4566
```

Docker runtime DynamoDB endpoint:

```text
http://localstack:4566
```

When running the app inside Docker, set:

```bash
AWS_DYNAMODB_ENDPOINT=http://localstack:4566
```

## API Contract

Base path:

```text
/music
```

Supported operations:

- `POST /music`: create a music record.
- `GET /music/{artist}/{song}`: find a music record.
- `PUT /music`: update a music record.
- `DELETE /music/{artist}/{song}`: delete a music record.

Request body shape:

```json
{
  "artist": "Daft Punk",
  "songTitle": "One More Time",
  "year": 2000
}
```

## Coding Guidelines

- Keep the current layered structure: controller, service, repository, config.
- Do not put persistence logic in controllers.
- Keep request and response models in `entrypoint`.
- Keep DynamoDB entity annotations in `repository`.
- Keep AWS SDK configuration in `config`.
- Prefer Java records for simple request, response, and DTO objects when no mutable state is needed.
- Keep DynamoDB attribute names aligned with the table definition in `Docker/create-dynamo.sh`.
- Use constructor injection for Spring beans.
- Avoid introducing new frameworks unless there is a clear project need.

## Testing Guidelines

- Add or update tests when changing business logic, repository behavior, or API contracts.
- Run `./gradlew test` before finishing changes when the task touches Java code.
- Run `./gradlew clean build` when changing dependencies, Gradle configuration, Docker build behavior, or application startup configuration.

## Commit Guidelines

Use Conventional Commits for commit messages in this project.

Commit message format:

```text
<type>(optional-scope): <description>
```

Common types:

- `feat`: a new feature.
- `fix`: a bug fix.
- `docs`: documentation-only changes.
- `test`: adding or updating tests.
- `refactor`: code changes that neither fix a bug nor add a feature.
- `chore`: maintenance changes such as build, tooling, or configuration updates.

Examples:

```text
feat(music): add endpoint to list records
fix(repository): handle missing DynamoDB items
docs: update LocalStack setup instructions
test(service): cover music update flow
```

## Docker and LocalStack Notes

- `Docker/docker-compose.yaml` starts LocalStack.
- `Docker/create-dynamo.sh` creates the `Music` table during LocalStack startup.
- The table must keep these keys unless code and API behavior are intentionally changed:
  - `Artist`
  - `SongTitle`
- `Docker/Dockerfile` expects the application JAR to exist in `build/libs/`.
- Run `./gradlew clean build` before building the Docker image manually.

## Documentation Guidelines

- Update `README.md` when changing setup steps, public API behavior, dependency versions, or Docker commands.
- Update this `AGENTS.md` when changing project structure, conventions, or agent-specific workflow expectations.
