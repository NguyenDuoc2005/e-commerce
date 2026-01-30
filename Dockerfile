FROM eclipse-temurin:17-jre
WORKDIR /app

# jar được build ra trong BE/build/libs/
ARG JAR_FILE=BE/build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8386
ENV TZ=Asia/Bangkok
ENTRYPOINT ["java","-jar","/app/app.jar"]