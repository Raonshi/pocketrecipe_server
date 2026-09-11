# Repository Guidelines

## Project Structure & Module Organization

This Kotlin/Spring Boot API uses Supabase-compatible PostgreSQL. Application code lives in `src/main/kotlin/com/raondev/pocketrecipe`; recipe HTTP, service, API-client, persistence, and security code is organized by package. Runtime configuration is in `src/main/resources/application.yml`; ordered schema SQL lives in `db/schema`.

Tests mirror production packages in `src/test/kotlin`. Keep HTTP, external-client, and database integration tests separate. Use `src/test/resources` for test-only configuration and fixtures.

## Build, Test, and Development Commands

- `docker compose up --build` starts the local API and PostgreSQL services.
- `./gradlew test` runs unit and web tests; `docker compose run --rm test` also runs PostgreSQL integration tests.
- `./gradlew build` compiles, tests, and produces the application JAR.

Always use the Gradle Wrapper rather than a machine-wide Gradle version. CI runs `./gradlew clean build` on Java 21.

## Coding Style & Naming Conventions

Use Kotlin with four-space indentation, trailing commas in multiline declarations, and immutable `val` values by default. Use `PascalCase` for classes and `camelCase` for functions, variables, and JSON-mapped Kotlin properties. Preserve existing API names such as `/search-recipe`, `recipe_name`, and the legacy `fs_recipe` response key.

Keep controllers thin, place business rules in services, and isolate database access behind repository interfaces. Add a numbered schema SQL file instead of editing an applied file.

## Testing Guidelines

Use MockMvc for HTTP contracts and MockWebServer for Food Safety API behavior. Run PostgreSQL integration tests with `docker compose run --rm test`. Cover authentication, ownership, validation failures, missing recipes, and external-service failures.

## Commit, Pull Request, and Security Guidelines

Use short imperative commit subjects, such as `add recipe migration` or `fix recipe search`. Pull requests should describe API or schema changes, list validation commands, and include example requests/responses when the client contract changes.

Never commit Supabase credentials, Food Safety keys, or service-account files. `.env.example` is limited to local Docker Compose defaults; production values must come from a secret manager.
