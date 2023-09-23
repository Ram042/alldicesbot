FROM docker.io/library/eclipse-temurin:17-jdk as build
RUN mkdir /opt/src
COPY ./ /opt/src
RUN chmod +x /opt/src/gradlew && \
    /opt/src/gradlew --version
RUN cd /opt/src && \
    chmod +x gradlew && \
    ./gradlew installDist

FROM docker.io/library/eclipse-temurin:17-jre
COPY --from=build /opt/src/build/install/alldicesbot/lib/* /opt/app
CMD /opt/app
ENTRYPOINT ["java", "-classpath",  "/opt/app/*", "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener","ru.ramlabs.alldicesbot.Main"]