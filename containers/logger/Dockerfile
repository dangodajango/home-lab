FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src src

RUN mvn clean install

FROM amazoncorretto:21 AS run

WORKDIR /app
COPY --from=build /app/target/*.jar logger.jar

ENTRYPOINT ["java", "-jar", "logger.jar"]
CMD ["nlp=10", "ltso=true"]