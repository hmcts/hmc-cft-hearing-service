FROM adoptopenjdk:11-jre-hotspot as builder
ARG JAR_FILE=build/libs/hmc-cft-hearing-service.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

ARG APP_INSIGHTS_AGENT_VERSION=3.2.4
ARG PLATFORM=""
FROM hmctspublic.azurecr.io/base/java${PLATFORM}:11-distroless
USER hmcts

# The following layer ARGs are only needed to stop Fortify flagging an issue with the COPY instructions
ARG DIR_LAYER_APPLICATION=application/
ARG DIR_LAYER_DEPENDECIES=dependencies/
ARG DIR_LAYER_SPRING_BOOT_LOADER=spring-boot-loader/
ARG DIR_LAYER_SNAPSHOT_DEPENDENCIES=snapshot-dependencies/

COPY --from=builder ${DIR_LAYER_APPLICATION} /opt/app/
COPY --from=builder ${DIR_LAYER_DEPENDECIES} /opt/app/
# Add 'CMD true or RUN true' if consecutive COPY commands are failing in case (intermittently).
# See https://github.com/moby/moby/issues/37965#issuecomment-771526632
COPY --from=builder ${DIR_LAYER_SPRING_BOOT_LOADER} /opt/app/
COPY --from=builder ${DIR_LAYER_SNAPSHOT_DEPENDENCIES} /opt/app/

EXPOSE 4561
ENTRYPOINT ["/usr/bin/java", "org.springframework.boot.loader.JarLauncher"]
