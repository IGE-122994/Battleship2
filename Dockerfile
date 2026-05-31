FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/BattleshipGamePlayer-2.0.jar app.jar

ENTRYPOINT ["java", "-cp", "app.jar", "battleship.Main"]