from docker.io/eclipse-temurin:17-alpine
run mkdir /opt/app
ENTRYPOINT ["java", "-classpath",  "/opt/app/*", "ru.ramlabs.Main"]
copy build/install/alldicesbot/lib/* /opt/app
