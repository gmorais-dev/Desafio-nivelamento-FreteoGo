FROM gradle:7.6.4-jdk8 AS build

WORKDIR /app

COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle gradle
COPY src src

RUN chmod +x gradlew && ./gradlew --no-daemon clean war

FROM tomcat:9.0-jdk8-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/build/libs/*.war /usr/local/tomcat/webapps/SISTEMA-FRETES.war

EXPOSE 8080
