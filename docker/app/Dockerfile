FROM adoptopenjdk/openjdk11:alpine-slim

LABEL io.k8s.description="Platform for Java (fatjar) runtime" \
      io.openshift.expose-services="8080:http" \
      io.openshift.tags="java,kotlin,microservices,fatjar"

WORKDIR /deployments

# Application
COPY build/order-enqueuing-service.jar .
# NewRelic java agent
COPY build/newrelic.jar /opt/newrelic/newrelic.jar
COPY build/newrelic.yml /opt/newrelic/newrelic.yml

EXPOSE 8080

CMD ["java", "-jar", "order-enqueuing-service.jar", "com.phizzard.es.AppKt", "src/main/resources/config.yml"]
