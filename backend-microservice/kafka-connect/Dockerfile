FROM confluentinc/cp-kafka-connect:8.0.0

ARG DEBEZIUM_MYSQL_CONNECTOR_VERSION=3.2.6-3
ARG ELASTICSEARCH_SINK_CONNECTOR_VERSION=15.1.3

# Reproducible builds: never pull a moving connector version into CDC.
RUN confluent-hub install --no-prompt debezium/debezium-connector-mysql:${DEBEZIUM_MYSQL_CONNECTOR_VERSION} \
    && confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:${ELASTICSEARCH_SINK_CONNECTOR_VERSION}
