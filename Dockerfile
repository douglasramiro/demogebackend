FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/orderhub-1.0.0.jar /app/app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
