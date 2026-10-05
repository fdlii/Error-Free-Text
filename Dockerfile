FROM gradle:8.14-jdk17 AS build
WORKDIR /build

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY src src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17.0.18_8-jdk
WORKDIR /app
COPY --from=build /build/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]