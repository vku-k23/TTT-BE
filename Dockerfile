FROM maven:3.8.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn install -DskipTests=true

FROM eclipse-temurin:17-jre-alpine
WORKDIR /run

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
    echo "Asia/Ho_Chi_Minh" > /etc/timezone && \
    apk del tzdata

COPY --from=build /app/target/*.jar /run/app.jar

EXPOSE 8888

ENV JAVA_OPTIONS="-Xmx2048m -Xms256m"
ENTRYPOINT exec java $JAVA_OPTIONS -jar /run/app.jar