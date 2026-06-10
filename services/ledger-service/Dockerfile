FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace

COPY settings.gradle build.gradle ./
COPY src ./src

RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S ledger && adduser -S ledger -G ledger
USER ledger

COPY --from=build /workspace/build/libs/*.jar /app/ledger-service.jar

EXPOSE 8080 9090

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"'

ENTRYPOINT ["java", "-jar", "/app/ledger-service.jar"]

