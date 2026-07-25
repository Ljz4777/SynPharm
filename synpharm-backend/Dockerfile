FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/synpharm-backend-1.0.0.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]