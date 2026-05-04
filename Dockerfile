FROM eclipse-temurin:21-jdk AS build

WORKDIR /src

COPY DNS ./DNS
COPY Middleware ./Middleware
COPY Requests ./Requests
COPY Demo ./Demo
COPY Server ./Server

RUN mkdir /app && \
    javac -encoding UTF-8 -d /app/classes \
        DNS/*.java \
        Middleware/*.java \
        Requests/*.java \
        Demo/*.java \
        Server/protocol/*.java \
        Server/serverbase/*.java \
        Server/storage/*.java

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/classes ./classes

ENTRYPOINT ["java", "-cp", "/app/classes"]
CMD ["DemoClient", "middleware", "8000", "scenario"]
