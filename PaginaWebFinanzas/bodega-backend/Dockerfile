# Cuaderno de Fiado Digital - backend
# Construcción multi-etapa: compila con Maven y ejecuta con un JRE ligero.
# Variables necesarias en producción (Render):
#   DATABASE_URL (host), DATABASE_PORT, DATABASE_NAME, DATABASE_USER, DATABASE_PASSWORD,
#   JWT_SECRET (mínimo 32 caracteres), CORS_ORIGENES (URL del frontend), PORT.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:25-jre AS runtime
ENV SPRING_PROFILES_ACTIVE=prod
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
