# URL Shortener (Spring Boot)

Educational, production-shaped URL shortener in **Java 17 + Spring Boot 3**. Run locally on free H2 storage, or point at free-tier PostgreSQL (Neon, Supabase, Railway).

**System design deep-dive:**

| Format | File |
|--------|------|
| Markdown (source) | [docs/SYSTEM_DESIGN.md](docs/SYSTEM_DESIGN.md) |
| HTML (diagrams, print-friendly) | [docs/SYSTEM_DESIGN.html](docs/SYSTEM_DESIGN.html) |

Regenerate HTML after editing the Markdown:

```bash
python3 scripts/render-design-doc.py
# Open docs/SYSTEM_DESIGN.html in a browser → "Save as PDF" for PDF
```

## Prerequisites

- JDK 17+ (`java -version`)
- **Maven is optional** — this repo includes the **Maven Wrapper** (`./mvnw`).

## Quick start (local, zero external DB)

```bash
cd url-shortener
./mvnw spring-boot:run
```

Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Create a link

```bash
curl -s -X POST http://localhost:8080/api/v1/links \
  -H 'Content-Type: application/json' \
  -d '{"longUrl":"https://spring.io/projects/spring-boot"}' | jq
```

### Redirect

```bash
curl -i "http://localhost:8080/<shortCode>"
```

### Stats

```bash
curl -s "http://localhost:8080/api/v1/links/<shortCode>/stats" | jq
```

H2 files are stored under `url-shortener/.data/` (profile `local`, default).

## PostgreSQL (docker-compose)

```bash
docker compose up -d
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

JDBC defaults: `jdbc:postgresql://localhost:5432/urlshortener` / user `urlshortener` / password `urlshortener`.

## Tests

```bash
./mvnw test
```

## Configuration

| Variable | Description |
|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | `local` (H2) or `postgres` |
| `APP_BASE_URL` | Public URL prefix in API responses |
| `DATABASE_URL` | JDBC URL when using `postgres` profile |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | DB credentials |
| `PORT` | HTTP port (default 8080) |

See `src/main/resources/application.yml` and `AppProperties`.

## Docker image

```bash
docker build -t url-shortener:local .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e APP_BASE_URL=http://localhost:8080 \
  url-shortener:local
```

## Free-tier deployment (Neon + Render)

Step-by-step: **[docs/DEPLOY.md](docs/DEPLOY.md)**

1. Create a **Neon** free PostgreSQL database (connection string).
2. Deploy on **Render** (Docker) from GitHub.
3. Env: `SPRING_PROFILES_ACTIVE=postgres`, `DATABASE_URL`, `APP_BASE_URL=https://<your-host>`.

## API summary

| Endpoint | Description |
|----------|-------------|
| `POST /api/v1/links` | Create short URL |
| `GET /api/v1/links/{code}` | Link metadata |
| `GET /api/v1/links/{code}/stats` | Click analytics |
| `GET /{code}` | HTTP 302 redirect |
| `GET /actuator/health` | Health probe |

## Project layout

```
src/main/java/com/example/urlshortener/
  config/       Caffeine cache, rate limit, properties
  domain/       JPA entities
  repository/   Spring Data
  service/      Business logic
  web/          REST + redirect controllers
docs/
  SYSTEM_DESIGN.md
```

## License

MIT (educational use).
