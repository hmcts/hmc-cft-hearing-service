ARG PLATFORM=""
# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.5.4
FROM hmctsprod.azurecr.io/imported/eclipse-temurin${PLATFORM}:21 AS builder
WORKDIR /builder
ARG JAR_FILE=build/libs/hmc-cft-hearing-service.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted


FROM hmctsprod.azurecr.io/base/java${PLATFORM}:21-distroless
USER hmcts

COPY lib/applicationinsights.json /opt/app

# The following layer ARGs are only needed to stop Fortify flagging an issue with the COPY instructions
ARG DIR_LAYER_APPLICATION=/builder/extracted/application/
ARG DIR_LAYER_DEPENDECIES=/builder/extracted/dependencies/
ARG DIR_LAYER_SPRING_BOOT_LOADER=/builder/extracted/spring-boot-loader/
ARG DIR_LAYER_SNAPSHOT_DEPENDENCIES=/builder/extracted/snapshot-dependencies/

COPY --from=builder ${DIR_LAYER_APPLICATION} /opt/app/
COPY --from=builder ${DIR_LAYER_DEPENDECIES} /opt/app/
# Add 'CMD true or RUN true' if consecutive COPY commands are failing in case (intermittently).
# See https://github.com/moby/moby/issues/37965#issuecomment-771526632
COPY --from=builder ${DIR_LAYER_SPRING_BOOT_LOADER} /opt/app/
COPY --from=builder ${DIR_LAYER_SNAPSHOT_DEPENDENCIES} /opt/app/

EXPOSE 4561
ENTRYPOINT ["/usr/bin/java", "org.springframework.boot.loader.launch.JarLauncher"]
