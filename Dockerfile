FROM  gradle:jdk17-corretto AS build

WORKDIR /app
COPY . /app

RUN gradle buildFatJar --no-daemon

FROM openjdk:17-jdk-slim

WORKDIR /app
COPY --from=build /app/build/libs/*-fat.jar app.jar

EXPOSE 8080
ENV PORT=8080
ENV DATABASE_ENVIRONMENT="prod"

CMD ["java", "-jar", "app.jar"] 