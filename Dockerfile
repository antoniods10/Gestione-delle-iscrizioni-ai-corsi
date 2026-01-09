FROM maven:3.9-eclipse-temurin-23 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY ./ ./

RUN mvn clean package -DskipTests

FROM eclipse-temurin:23-jre

WORKDIR /newunimol

COPY --from=build /app/target/NewUnimol-1.0.0-SNAPSHOT.jar NewUnimol.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "NewUnimol.jar" ]