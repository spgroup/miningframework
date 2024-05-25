FROM openjdk:8 as build

WORKDIR /usr/src/miningframework

COPY . .

RUN ./gradlew installDist

FROM openjdk:8

WORKDIR /usr/src/miningframework

COPY --from=build /usr/src/miningframework/build .

ENTRYPOINT ["./install/miningframework/bin/miningframework"]
