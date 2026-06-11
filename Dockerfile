FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

ARG MODULE=mcp-server

COPY pom.xml ./
COPY legacy-api/pom.xml legacy-api/pom.xml
COPY mcp-server/pom.xml mcp-server/pom.xml
COPY worker-service/pom.xml worker-service/pom.xml
COPY mock-legacy-service/pom.xml mock-legacy-service/pom.xml
COPY legacy-api/src legacy-api/src
COPY mcp-server/src mcp-server/src
COPY worker-service/src worker-service/src
COPY mock-legacy-service/src mock-legacy-service/src

RUN mvn -q -pl ${MODULE} -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

ARG MODULE=mcp-server
ARG JAR_FILE=banking-agent-mcp-server-0.1.0-SNAPSHOT.jar

COPY --from=build /workspace/${MODULE}/target/${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
