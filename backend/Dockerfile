# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src
RUN chmod +x mvnw && ./mvnw -B -DskipTests package \
    && cp target/renko-saas-pos-system-*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN useradd --system --create-home --uid 10001 appuser
USER appuser

COPY --from=build /workspace/app.jar /app/app.jar

ENV SERVER_PORT=5000
EXPOSE 5000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
