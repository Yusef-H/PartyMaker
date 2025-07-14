FROM openjdk:11-jre-slim

WORKDIR /app/server

# העתק את כל קבצי השרת
COPY . .

# ודא של-gradlew יש הרשאות ריצה
RUN chmod +x gradlew

# בנה את ה-JAR
RUN ./gradlew bootJar

# העתק את ה-JAR שנבנה לשורש ה-image
WORKDIR /app
COPY --from=0 /app/server/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 