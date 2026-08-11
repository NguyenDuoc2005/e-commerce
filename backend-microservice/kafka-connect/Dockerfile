FROM confluentinc/cp-kafka-connect:7.6.1

RUN confluent-hub install --no-prompt debezium/debezium-connector-mysql:2.5.4 \
    && confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:14.0.11
