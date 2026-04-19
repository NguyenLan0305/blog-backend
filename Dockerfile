# ---------- BUILD STAGE ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom trước để cache dependency
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# ---------- RUN STAGE ----------
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy jar từ stage build
COPY --from=builder /app/target/*.jar app.jar

# Render dùng PORT env
ENV PORT=8080

EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]