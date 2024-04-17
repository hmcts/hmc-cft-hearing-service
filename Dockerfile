ARG PLATFORM=""
ARG APP_INSIGHTS_AGENT_VERSION=3.4.13

FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless
USER hmcts
LABEL maintainer="https://github.com/hmcts/hmc-cft-hearing-service"

COPY build/libs/hmc-cft-hearing-service.jar /opt/app/
COPY lib/applicationinsights.json /opt/app

EXPOSE 4561
CMD ["hmc-cft-hearing-service.jar"]
