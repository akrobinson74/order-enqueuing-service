FROM java:8-jre-alpine

ARG VERSION=0.14.14

EXPOSE 9324

ADD https://s3-eu-west-1.amazonaws.com/softwaremill-public/elasticmq-server-${VERSION}.jar /elasticmq-server.jar

COPY elasticmq.conf /etc/elasticmq/elasticmq.conf
COPY logback.xml /etc/elasticmq/logback.xml

VOLUME /etc/elasticmq

CMD ["java", "-Dlogback.configurationFile=/etc/elasticmq/logback.xml", "-Djava.net.preferIPv4Stack=true", "-Dconfig.file=/etc/elasticmq/elasticmq.conf", "-jar", "/elasticmq-server.jar"]
