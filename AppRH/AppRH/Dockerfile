FROM eclipse-temurin:22-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw

RUN ./mvnw --batch-mode dependency:go-offline

COPY src src

RUN ./mvnw --batch-mode clean package -DskipTests

FROM eclipse-temurin:22-jre AS runtime

WORKDIR /app

RUN groupadd \
        --system \
        apprh \
    && useradd \
        --system \
        --gid apprh \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        apprh

COPY --from=build --chown=apprh:apprh /app/target/*.jar /app/app.jar

USER apprh

EXPOSE 8081

ENV PORTA=8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]