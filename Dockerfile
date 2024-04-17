ARG PLATFORM=""
ARG JAR_FILE=build/libs/hmc-cft-hearing-service.jar
ARG APP_INSIGHTS_AGENT_VERSION=3.4.13

FROM eclipse-temurin${PLATFORM}:17 as builder
COPY ${JAR_FILE} application.jar

FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless
LABEL maintainer="https://github.com/hmcts/hmc-cft-hearing-service"
USER hmcts

COPY lib/applicationinsights.json /opt/app/
COPY --from=builder application.jar /opt/app/

EXPOSE 4561
ENTRYPOINT ["java", "-jar", "/opt/app/application.jar"]
