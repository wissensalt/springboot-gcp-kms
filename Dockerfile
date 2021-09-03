FROM openjdk:8-alpine3.9

COPY build/libs/springboot-gcp-kms.jar /app/app.jar

WORKDIR /app

ENTRYPOINT ["java", "-Djava.security.edg=file:/dev/./urandom", "-jar", "app.jar"]