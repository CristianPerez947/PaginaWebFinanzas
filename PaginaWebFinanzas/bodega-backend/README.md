# Backend — Cuaderno de Fiado Digital

API REST en Java 25 + Spring Boot 4 (DDD). Requisitos: JDK 25+ y Maven 3.9+.

## Levantar

```bash
# Sin PostgreSQL (base en memoria, con datos de demostración)
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"

# Con PostgreSQL (cree antes la base "bodega" y defina DATABASE_PASSWORD; ver .env.example)
mvn spring-boot:run
```

- API: <http://localhost:8080>
- Swagger: <http://localhost:8080/swagger-ui.html>

## Otros comandos

```bash
mvn test                    # pruebas de las fórmulas financieras
mvn clean package           # genera target/plataforma-1.0.0.jar
java -jar target/plataforma-1.0.0.jar --spring.profiles.active=h2
```

Usuarios de demostración y despliegue: ver el [README principal](../README.md).
