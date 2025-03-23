FROM xiyo/ffmpeg-base:latest
WORKDIR /app

COPY build/libs/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]