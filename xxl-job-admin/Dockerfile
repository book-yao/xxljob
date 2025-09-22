# arm环境
FROM arm64v8/openjdk:8-jre-slim
# 非arm架构
#FROM openjdk:8-jre-slim
#FROM openjdk:8-jdk-alpine
MAINTAINER xuxueli

ENV XXL_PARAMS=""

ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD target/xxl-job-admin-*.jar /app.jar

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /app.jar $XXL_PARAMS"]