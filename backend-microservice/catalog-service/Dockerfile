FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew :catalog-service:bootJar --no-daemon --max-workers=1

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/catalog-service/build/libs/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
