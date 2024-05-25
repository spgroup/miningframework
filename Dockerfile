FROM openjdk:8 as build

WORKDIR /usr/src/miningframework

COPY . .

RUN ./gradlew installDist

FROM openjdk:8

WORKDIR /usr/local/bin/miningframework

COPY --from=build /usr/src/miningframework/build .

WORKDIR /usr/local/bin/miningframework/install/miningframework/bin

ENTRYPOINT ["./miningframework"]
