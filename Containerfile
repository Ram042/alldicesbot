FROM docker.io/library/eclipse-temurin:17-jre-alpine
ENTRYPOINT ["java", "-classpath",  "/opt/app/*", "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener","ru.ramlabs.alldicesbot.Main"]
RUN mkdir /opt/app
COPY build/install/alldicesbot/lib/* /opt/app
