#stage1
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

#stage2
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/chatbot-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8093

ENV APP_INTERNAL_SECRET=your_app_internal_secret

ENTRYPOINT ["java", "-jar", "app.jar"]