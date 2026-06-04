# Step 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the root pom and all modules
COPY pom.xml .
COPY expense-app ./expense-app
COPY expense-infrastructure ./expense-infrastructure
COPY expense-domain ./expense-domain

# Build only expense-app and its dependencies
RUN mvn clean package -pl expense-app -am -DskipTests


# Step 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S springgroup && adduser -S springuser -G springgroup

# Copy the built jar from the build stage's module-3 target folder
COPY --from=build /app/expense-app/target/*.jar app.jar
RUN chown springuser:springgroup app.jar
USER springuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]