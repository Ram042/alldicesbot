FROM docker.io/library/eclipse-temurin:17-jre
COPY build/install/alldicesbot/lib/* /opt/app
CMD /opt/app
ENTRYPOINT ["java", "-classpath",  "/opt/app/*", "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener","ru.ramlabs.alldicesbot.Main"]