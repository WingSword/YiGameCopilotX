FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY src/main/java ./src/main/java
COPY src/main/resources ./src/main/resources
RUN mkdir classes && find src/main/java -name '*.java' > sources.txt && javac --release 17 -encoding UTF-8 -d classes @sources.txt && jar --create --file server.jar --main-class org.walks.rooms.RoomServer -C classes . -C src/main/resources .

FROM eclipse-temurin:21-jre
RUN groupadd --gid 10001 rooms && useradd --uid 10001 --gid rooms --no-create-home rooms && mkdir -p /app/data && chown -R rooms:rooms /app
WORKDIR /app
COPY --from=build --chown=rooms:rooms /src/server.jar /app/server.jar
USER rooms
ENV PORT=8080 DATA_DIR=/app/data
EXPOSE 8080
ENTRYPOINT ["java", "-Xms64m", "-Xmx256m", "-Dsun.net.httpserver.maxReqTime=15", "-Dsun.net.httpserver.maxRspTime=15", "-jar", "/app/server.jar"]
