FROM adoptopenjdk/openjdk11:jdk-11.0.3.7

WORKDIR /deployments

# Application
COPY build/wait-for-it.sh .
COPY build/config-"${ENV:-stage}".yml /opt/config.yml
COPY build/order-enqueuing-service.jar .
# NewRelic java agent
COPY build/newrelic.jar /opt/newrelic/newrelic.jar
COPY build/newrelic.yml /opt/newrelic/newrelic.yml

EXPOSE 9080

CMD ["java", "-jar", "order-enqueuing-service.jar", "/opt/config.yml"]
