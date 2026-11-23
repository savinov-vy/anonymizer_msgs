FROM maven:3.9.6-eclipse-temurin-21 AS build-container

USER root

RUN mkdir -p /home/service

COPY . /home/service

WORKDIR /home/service

RUN mvn install -DskipTests



FROM eclipse-temurin:21-jre-jammy

RUN mkdir -p /home/service

COPY --from=build-container /home/service/target/wrapper-0.0.1.jar /home/service

CMD ["java", "-jar", "/home/service/wrapper-0.0.1.jar"]