FROM amazoncorretto:21-alpine-jdk

# 필요한 패키지 업데이트 및 ffmpeg 설치
RUN apk update && \
    apk add --no-cache ffmpeg

# ffmpeg 설치 확인
RUN ffmpeg -version