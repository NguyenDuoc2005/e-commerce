FROM confluentinc/cp-kafka-connect:8.0.0

RUN confluent-hub install --no-prompt debezium/debezium-connector-mysql:latest \
    && confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:latest
