FROM adoptopenjdk/openjdk11:slim

WORKDIR /workdir

# Application
COPY build/wait-for-it.sh .
COPY build/config-dev.yml /opt/config.yml
COPY build/order-enqueuing-service.jar .

# NewRelic java agent
ADD build/newrelic-5.7.0/ /opt/newrelic/
RUN mkdir -p /opt/newrelic/logs

EXPOSE 80

CMD ["java", "-javaagent:/opt/newrelic/newrelic.jar", "-jar", "order-enqueuing-service.jar", "/opt/config.yml"]
