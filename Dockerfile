
# ========== STAGE 1: BUILD ==========
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Χρησιμοποιούμε cache για dependencies
COPY ../../../pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Προσθέτουμε τον υπόλοιπο κώδικα και χτίζουμε
COPY ../.. ./src
RUN mvn -q -DskipTests clean package

# ========== STAGE 2: RUNTIME ==========
FROM eclipse-temurin:21-jre-alpine
# Για healthcheck με curl (προαιρετικό)
RUN apk add --no-cache curl
# non-root χρήστης
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

# Αντιγραφή του jar (Spring Boot repackage παράγει ένα *.jar)
COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 -XX:+ExitOnOutOfMemoryError"
ENV SERVER_PORT=8080

# Προαιρετικό healthcheck προς actuator (αν έχεις starter-actuator και ανοικτό endpoint)
# HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
#   CMD curl -sf http://localhost:${SERVER_PORT}/actuator/health | grep '"status":"UP"' || exit 1

USER spring
ENTRYPOINT ["java","-jar","/app/app.jar"]
