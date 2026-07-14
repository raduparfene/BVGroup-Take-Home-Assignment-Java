#!/bin/sh

set -eu

/opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
  --create \
  --if-not-exists \
  --topic "$KAFKA_TOPIC_NAME" \
  --partitions "$KAFKA_TOPIC_PARTITIONS" \
  --replication-factor 1
