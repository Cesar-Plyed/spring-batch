# spring-batch
A simple Spring Boot application, using Spring Batch.

Development of batch processing applications using Spring Batch, implementing data read, transformation, and write (ETL) flows with error handling, fault tolerance, and execution history persistence.

---

## File Structure

```
src/
├── main/
│   ├── java/com/ejemplo/batch/
│   │   ├── SpringBatchDemoApplication.java   ← Punto de entrada
│   │   ├── config/
│   │   │   └── BatchConfig.java              ← Configuración del Job/Step
│   │   ├── model/
│   │   │   ├── UsuarioCSV.java               ← Modelo del CSV (entrada)
│   │   │   ├── Usuario.java                  ← Entidad JPA (salida/BD)
│   │   │   └── UsuarioRepository.java        ← Acceso a BD
│   │   ├── processor/
│   │   │   └── UsuarioProcessor.java         ← Validación y transformación
│   │   └── listener/
│   │       └── JobCompletionListener.java    ← Reporte final
│   └── resources/
│       ├── application.properties            ← Configuración
│       └── usuarios.csv                      ← Datos de entrada
└── test/
    └── SpringBatchDemoApplicationTests.java  ← Tests de integración
```

---
## Architectura

```
            ┌───────────────────────────────────────────────────────┐
            │                 JOB: importUsersJob                   │
            │                                                       │
            │  ┌─────────────────────────────────────────────────┐  │
            │  │           STEP: readAndSaveUserStep             │  │
            │  │                                                 │  │
            │  │        CHUNK SIZE = 3 (process 3 by 3)          │  │
            │  │                                                 │  │
            │  │  ┌──────────┐  ┌──────────────┐  ┌───────────┐  │  │
            │  │  │  READER  │→ │  PROCESSOR   │→ │  WRITER   │  │  │
            │  │  │          │  │              │  │           │  │  │
            │  │  │ Read CSV │  │ Validates    |  |  Save in  |  |  |
            |  |  | Guarda en│  │ Email and    │  │  H2 data  │  |  |
            │  │  │ each     │  │ Transform    │  │  base     │  │  │
            │  │  │ line     │  │ Enriquece    │  │           │  │  │
            │  │  └──────────┘  └──────────────┘  └───────────┘  │  │
            │  └─────────────────────────────────────────────────┘  │
            └───────────────────────────────────────────────────────┘
```

---

## Key Concepts learned

| Concepto | Clase | Qué hace |
|----------|-------|----------|
| **Job** | `BatchConfig.importarUsuariosJob` | The prosses was completed |
| **Step** | `BatchConfig.leerYGuardarUsuariosStep` | A stage of the Job |
| **ItemReader** | `FlatFileItemReader` | Read the CSV file row by row |
| **ItemProcessor** | `UsuarioProcessor` | Validate and transform data |
| **ItemWriter** | `RepositoryItemWriter` | Save to the database using JPA |
| **Chunk** | `chunk(3, ...)` | Process in batches of 3 |
| **Listener** | `JobCompletionListener` | Hooks before/after the job |
| **Skip** | `.skip(Exception.class)` | Be tolerant of mistakes without giving up |

---

## How execute this

### Requisitos
- Java 21+
- Maven 3.8+

### Run
```bash
# Clone/unzip the project
cd spring-batch-demo

# Compile and run
mvn spring-boot:run
```

### View the H2 database
While the application is running, open the following URL in your browser:
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:batchdb
Username: sa
Password: (blank)
```

### Run tests
```bash
mvn test
```

---

## Data Flow

```
users.csv (12 rows)
        │
        ▼
  FlatFileItemReader
  Reads each line → UserCSV
        │
        ▼
  UserProcessor
  Valid email    → Enriched user (with category, timestamp)
  Invalid email  → null (DISCARED, not saved)
        │
        ▼
  RepositoryItemWriter
  Saves User to H2 table
        │
        ▼
  Result: ~10 users saved
  (2 discarded due to invalid email)
```

---

##  What Can You Try?

1. **Change the chunk size** in `BatchConfig` (try 1, 5, or 100)
2. **Add more validations** in `UserProcessor`
3. **Add a second Step** that sends a summary email
4. **Change H2 to MySQL/PostgreSQL** in `application.properties`
5. **Schedule the Job** with `@Scheduled` to run every night

---

## Next Steps

- [] Add `@Scheduled` for automatic execution
- [ ] Read from a REST API instead of a CSV
- [✓] Process multiple files with `MultiResourceItemReader`
- [ ] Parallelize with Partitioning for millions of records
- [ ] Add metrics with Spring Actuator