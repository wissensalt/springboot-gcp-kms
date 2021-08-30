FROM openjdk:8-alpine3.9

COPY gcp/keen-quest-317103-3bd712a1d235.json /app/gcp.json
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/gcp.json

COPY build/libs/springboot-gcp-kms.jar /app/app.jar

WORKDIR /app

RUN echo $GOOGLE_APPLICATION_CREDENTIALS

ENTRYPOINT ["java", "-Djava.security.edg=file:/dev/./urandom", "-jar", "app.jar"]