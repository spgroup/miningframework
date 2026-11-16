FROM amazoncorretto:17 AS build

WORKDIR /usr/src/miningframework

COPY . .

RUN ./gradlew installDist

FROM amazoncorretto:17

WORKDIR /usr/src/miningframework

RUN yum -y update
RUN yum -y install git

COPY --from=build /usr/src/miningframework/build /usr/local/bin/miningframework
RUN chmod +x /usr/local/bin/miningframework/install/miningframework/bin/miningframework

ENV PATH="/usr/local/bin/miningframework/install/miningframework/bin:${PATH}"

ENTRYPOINT ["miningframework"]
