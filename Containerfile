from docker.io/eclipse-temurin:17-alpine
run mkdir /opt/app
ENTRYPOINT ["java", "-classpath",  "/opt/app/*", "ru.ramlabs.alldicesbot.Main", \
"-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener"]
copy build/install/alldicesbot/lib/* /opt/app
