FROM eclipse-temurin:17-jre
WORKDIR /app
COPY backend.jar app.jar
EXPOSE 8386
ENV TZ=Asia/Bangkok
ENTRYPOINT ["java","-jar","/app/app.jar"]
