FROM docker.elastic.co/logstash/logstash:7.2.0

RUN rm -f /usr/share/logstash/pipeline/logstash.conf
RUN rm -f /usr/share/logstash/config/logstash-sample.conf

COPY jvm.options /usr/share/logstash/config/
COPY log4j2.properties /usr/share/logstash/config/
COPY logstash.conf /usr/share/logstash/config/

CMD ["/usr/share/logstash/bin/logstash", "-f", "/usr/share/logstash/config/logstash.conf"]