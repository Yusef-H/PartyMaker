#Step1: build
FROM openjdk:11-jdk-slim AS build

WORKDIR /app/server

COPY . .

RUN chmod +x gradlew
RUN ./gradlew bootJar

# Step2: Run
FROM openjdk:11-jre-slim

WORKDIR /app

# Step3: Copy the jar from previous step
COPY --from=build /app/server/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 