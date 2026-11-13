# Deploy to free cloud (Neon + Render)

## Overview

| Step | Service | Cost |
|------|---------|------|
| 1 | [Neon](https://neon.tech) PostgreSQL | Free tier |
| 2 | [Render](https://render.com) Docker web service | Free tier (cold starts) |

## 1 — Neon database

1. Sign in → **New project**.
2. Copy the **connection string** (starts with `postgresql://`).
3. Keep user, password, host, and database name handy.

You may set **one** env var on Render:

```bash
DATABASE_URL=postgresql://USER:PASSWORD@HOST/DBNAME?sslmode=require
```

Or use JDBC form:

```bash
DATABASE_URL=jdbc:postgresql://HOST/DBNAME?sslmode=require
DATABASE_USERNAME=...
DATABASE_PASSWORD=...
```

## 2 — Render web service

### Option A — Dashboard (recommended first time)

1. Push this folder to a Git repo Render can access (GitHub).
2. Render → **New** → **Web Service** → connect repo.
3. **Root directory:** `url-shortener` (if app lives in monorepo).
4. **Environment:** Docker (uses `Dockerfile`).
5. **Health check path:** `/actuator/health`
6. **Environment variables:**

| Key | Value |
|-----|--------|
| `SPRING_PROFILES_ACTIVE` | `postgres` |
| `DATABASE_URL` | Neon connection string |
| `APP_BASE_URL` | `https://<your-service>.onrender.com` |

7. **Create Web Service** → wait for build (~5–10 min first time).

### Option B — Script (needs API key)

1. Render → Account Settings → **API Keys** → create key.
2. Export: `export RENDER_API_KEY='rnd_...'`
3. Set Neon URL: `export DATABASE_URL='postgresql://...'`
4. Run from `url-shortener`:

```bash
./scripts/deploy-to-render.sh
```

## 3 — Verify

```bash
export BASE=https://<your-service>.onrender.com
curl -s "$BASE/actuator/health"
curl -s -X POST "$BASE/api/v1/links" \
  -H 'Content-Type: application/json' \
  -d '{"longUrl":"https://example.com"}' | jq
```

## Limits (free tier)

- Render sleeps after ~15 min idle; first request wakes the JVM (slow).
- Neon: storage and connection limits (fine for learning).
- Use **postgres** profile only — H2 file storage is lost on redeploy.
