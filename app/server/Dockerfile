FROM openjdk:11-jre-slim

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY src/main/resources/firebase-service-account.json /app/firebase-service-account.json

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 