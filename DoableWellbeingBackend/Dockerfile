# ---------- build stage ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew

COPY src ./src


RUN ./gradlew clean bootJar --no-daemon

# ---------- run stage ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
