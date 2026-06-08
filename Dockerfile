# Build stage
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Dfile.encoding=UTF-8

# Run stage
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /ap-graph
RUN apk add --no-cache libc6-compat libstdc++
COPY --from=build /app/target/ap-graph-*.jar ap-graph.jar
COPY html_files ./html_files

EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "ap-graph.jar"]