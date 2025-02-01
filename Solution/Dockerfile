# ---- Build stage ----
FROM maven:3.8.5-amazoncorretto-17 AS build
WORKDIR /app

# Copy pom.xml and resolve dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ---- Run stage ----
FROM amazoncorretto:17-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/receipt-processor.jar /app/receipt-processor.jar

# Expose port 8080
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "/app/receipt-processor.jar"]
