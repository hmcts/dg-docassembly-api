ARG APP_INSIGHTS_AGENT_VERSION=3.7.9

FROM hmctsprod.azurecr.io/base/java:25-distroless

USER hmcts
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/rpa-dg-docassembly.jar /opt/app/

CMD ["rpa-dg-docassembly.jar"]

EXPOSE 8080
