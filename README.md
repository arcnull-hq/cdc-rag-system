# cdc-rag-system

Semantic clinic search built on **Spring Boot 3.2**, **Spring AI**, and **PgVector**.

Clinics live in PostgreSQL as the relational source of truth. Every clinic is
also embedded into a PgVector store so it can be retrieved *by meaning* — ask
"where can I get a child vaccinated?" and get back the maternity/pediatric
clinic even if it never uses the word "vaccinated."

---

## Architecture

```
                 ┌─────────────────────────┐
   POST /clinics │  ClinicSearchController  │ GET /clinics/search
        ─────────▶          (rag)           ◀─────────
                 └───────────┬─────────────┘
                             │
              ┌──────────────┴───────────────┐
              ▼                               ▼
   ┌────────────────────┐        ┌──────────────────────────┐
   │  ClinicRepository  │        │  ClinicEmbeddingService   │
   │      (clinic)      │        │       (embedding)         │
   │   JPA → Postgres   │        │  VectorStore → PgVector   │
   │  SQL source of     │        │  derived embeddings,      │
   │  truth             │        │  search by meaning        │
   └────────────────────┘        └──────────────────────────┘
```

- **`clinic/`** — `Clinic` entity (authoritative row) + `ClinicRepository` (JPA).
- **`embedding/`** — `ClinicEmbeddingService` indexes clinics into PgVector and
  runs similarity search. Each document carries the `clinicId` in its metadata,
  so a semantic hit always traces back to the SQL row.
- **`rag/`** — `ClinicSearchController` exposes the HTTP API.

When a clinic is created, it is saved to SQL first, then embedded. SQL always
wins if the two stores disagree.

---

## Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL with the [`pgvector`](https://github.com/pgvector/pgvector) extension
- An OpenAI API key (or swap the starter for another embedding provider)

Spin up Postgres + pgvector quickly:

```bash
docker run -d --name cdcrag-pg \
  -e POSTGRES_DB=cdcrag \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  pgvector/pgvector:pg16
```

---

## Configuration

Secrets are read from the environment — nothing sensitive is committed.

| Variable          | Default     | Purpose                          |
|-------------------|-------------|----------------------------------|
| `OPENAI_API_KEY`  | *(empty)*   | Embedding model credentials      |
| `DB_HOST`         | `localhost` | Postgres host                    |
| `DB_PORT`         | `5432`      | Postgres port                    |
| `DB_NAME`         | `cdcrag`    | Database name                    |
| `DB_USER`         | `postgres`  | Database user                    |
| `DB_PASSWORD`     | `postgres`  | Database password                |
| `PORT`            | `8080`      | HTTP port                        |

```bash
export OPENAI_API_KEY=sk-...
```

---

## Run

```bash
mvn spring-boot:run
```

The PgVector schema is created automatically on first start
(`spring.ai.vectorstore.pgvector.initialize-schema=true`).

---

## API

### Add a clinic

```bash
curl -X POST http://localhost:8080/clinics \
  -H 'Content-Type: application/json' \
  -d '{
        "name": "Uhuru Family Health Centre",
        "location": "Nairobi, Kenya",
        "specialties": "pediatrics, maternity, immunization",
        "description": "Community clinic offering childhood immunizations, antenatal care, and well-baby checkups."
      }'
```

### Search clinics by meaning

```bash
curl "http://localhost:8080/clinics/search?query=where%20can%20I%20get%20my%20child%20vaccinated&topK=5"
```

Response:

```json
[
  {
    "clinicId": 1,
    "name": "Uhuru Family Health Centre",
    "location": "Nairobi, Kenya",
    "text": "Uhuru Family Health Centre — Nairobi, Kenya\nSpecialties: pediatrics, maternity, immunization\nCommunity clinic offering childhood immunizations, antenatal care, and well-baby checkups."
  }
]
```

---

## Milestones

- [x] **M0** — Project scaffold: entity, repo, embedding service, controller.
- [ ] **M1** — Backfill/reindex endpoint to embed existing clinics in bulk.
- [ ] **M2** — Generative answer layer: feed retrieved clinics to an LLM for a
      grounded natural-language response (full RAG, not just retrieval).
- [ ] **M3** — Metadata filtering (by location / specialty) on top of similarity.
- [ ] **M4** — Keep SQL and vector store in sync on update/delete.
