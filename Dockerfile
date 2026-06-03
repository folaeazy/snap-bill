# Step 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the root pom and all modules
COPY pom.xml .
COPY expense-app ./expense-app
COPY expense-infrastructure ./expense-infrastructure
COPY expense-domain ./expense-domain

# Build only module-3 and its dependencies
RUN mvn clean package -pl expense-app -am -DskipTests

# Step 2: Run the application
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage's module-3 target folder
COPY --from=build /app/expense-app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]