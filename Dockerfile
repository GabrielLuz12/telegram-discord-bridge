FROM eclipse-temurin:21

WORKDIR /app

COPY target/bot.jar bot.jar

CMD ["java", "-jar", "bot.jar"]
