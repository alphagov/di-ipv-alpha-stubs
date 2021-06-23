FROM gradle:jdk16@sha256:d31e12d105e332ec2ef1f31c20eac6d1467295487ac70e534e3c1d0ae4a0506e AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:15-slim@sha256:82fc670b1757068d299fb3f860201c5c97625b5ca351f903a6de33857398eb82

ENV PORT 8081
RUN addgroup --system --gid 1001 appgroup && adduser --system --uid 1001 appuser --gid 1001
RUN mkdir /app
RUN apt-get update && apt-get install -y curl
COPY --from=build \
	/home/gradle/src/build/distributions/src.tar \
	/app/

WORKDIR /app
RUN tar -xvf src.tar \
	&& rm src.tar

RUN chown -R appuser:appgroup /app/
USER appuser
EXPOSE $PORT

ENTRYPOINT ["./src/bin/src"]
LABEL project="di-ipv-orchestrator-stub"