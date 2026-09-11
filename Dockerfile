FROM gradle:8.14.3-jdk21 AS build
WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon
COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
RUN groupadd --system pocketrecipe && useradd --system --gid pocketrecipe --create-home pocketrecipe
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
USER pocketrecipe:pocketrecipe
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
