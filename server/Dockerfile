FROM openjdk:11-jdk-slim-buster
WORKDIR /server
COPY build.gradle .
COPY settings.gradle .

COPY build/libs/greenNare-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]