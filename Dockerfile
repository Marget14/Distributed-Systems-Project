# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests  # Skip tests for faster build; remove if needed

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar .
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "ls -la && exec java -jar $(ls *.jar | grep -v '\\.original$')"]
