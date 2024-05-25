FROM amazoncorretto:8 as build

WORKDIR /usr/src/miningframework

COPY . .

RUN ./gradlew installDist

FROM amazoncorretto:8

WORKDIR /usr/src/miningframework

COPY --from=build /usr/src/miningframework/build /usr/local/bin/miningframework
RUN chmod +x /usr/local/bin/miningframework/install/miningframework/bin/miningframework

ENV PATH="/usr/local/miningframework/install/miningframework/bin:${PATH}"

COPY docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh

ENTRYPOINT ["./docker-entrypoint.sh"]

CMD ["miningframework"]
