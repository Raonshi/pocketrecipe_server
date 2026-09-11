# PocketRecipe Server

Kotlin/Spring Boot API with PostgreSQL persistence and Supabase JWT authentication in production.

## Local development

```bash
docker compose up --build
```

Compose initializes PostgreSQL from `db/schema`, runs the API on `http://localhost:8080`, and uses the safe local settings in `.env.example`. Reset local data with `docker compose down -v`.

Run integration tests and secret scanning with:

```bash
docker compose run --rm test
docker compose run --rm security
```

## Production configuration

Copy `.env.example` to ignored `.env` only for local overrides. Production must inject database TLS settings, CORS origins, Supabase JWT issuer/JWKS URLs, and API keys from a secret manager. Apply ordered SQL files with `DATABASE_URL=... ./scripts/apply-schema.sh`; the server never changes schemas at startup.
