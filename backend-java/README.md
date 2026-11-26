# Vibe Snapshot — Java Spring Boot Backend

Drop-in Spring Boot + MongoDB replacement for `backend/` with identical REST endpoints, `/uploads/**` static serving, and `pixelmatch`-compatible image comparison.

## Prerequisites

- **Java 17+**
- **Maven 3.9+** (or Maven Wrapper `./mvnw` / `mvnw.cmd`)

## Environment Variables

Same environment configuration as the Node.js backend, plus MongoDB connection settings:

| Variable | Default | Description |
|---|---|---|
| `PORT` | `3001` | HTTP server port |
| `MONGODB_URI` | `mongodb://localhost:27017/vibe-snapshot` | MongoDB connection string (set in `backend-java/.env`) |
| `MONGODB_DB` | `vibe-snapshot` | MongoDB database name |
| `UPLOAD_DIR` | `uploads` | Directory for `golden/`, `submissions/`, and `diffs/` |

## Running

```bash
cd backend-java
mvn spring-boot:run
```

Or build and run the jar:

```bash
cd backend-java
mvn clean package
java -jar target/vibe-snapshot-backend-1.0.0.jar
```

The existing Vite frontend (`frontend/`) proxies `/api` and `/uploads` to `http://localhost:3001`, so no frontend or UI changes are needed.
