ARG APP_INSIGHTS_AGENT_VERSION=3.2.6

FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY build/libs/rpa-dg-docassembly.jar /opt/app/

CMD ["rpa-dg-docassembly.jar"]

EXPOSE 8080
