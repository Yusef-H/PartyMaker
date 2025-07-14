# שלב 1: Build
FROM openjdk:11-jre-slim AS build

WORKDIR /app/server

COPY . .

RUN chmod +x gradlew
RUN ./gradlew bootJar

# שלב 2: Run
FROM openjdk:11-jre-slim

WORKDIR /app

# העתק את ה-JAR מהשלב הקודם
COPY --from=build /app/server/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 