# arm版本需要重新拉arm版本的openjdk镜像
# docker pull openjdk:8-jre-slim --platform=linux/arm64/v8
# 修改build命令里面的配置--platform linux/arm64  / --platform linux/amd64
docker buildx build --platform linux/amd64 -t xuxueli/xxl-job-admin:latest --load .
docker save -o xxl-job-admin.tar xuxueli/xxl-job-admin:latest