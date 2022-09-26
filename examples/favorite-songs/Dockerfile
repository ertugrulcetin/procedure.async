FROM openjdk:8-alpine

COPY target/uberjar/favorite-songs.jar /favorite-songs/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/favorite-songs/app.jar"]
