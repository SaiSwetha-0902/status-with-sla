
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /app


COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine


RUN apk add --no-cache curl


RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

WORKDIR /app


COPY --from=builder /app/target/*.jar app.jar


RUN chown -R appuser:appuser /app


USER appuser

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1

CMD ["java", "-jar", "app.jar"]