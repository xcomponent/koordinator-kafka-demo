#!/usr/bin/env bash
set -o errexit
set -o nounset

BROKER=$1
LIMIT=$2
TOPIC=$3
KAFKA_TOOLS=$4

echo Creating topics..
$KAFKA_TOOLS/bin/kafka-topics.sh --create --zookeeper $BROKER:2181 --replication-factor 1 --partitions 6 --topic $TOPIC

echo Running consumers...
./run-consumer.sh $TOPIC $BROKER & 
P1=$!
echo Started consumer PID=$P1!

./run-consumer.sh $TOPIC $BROKER & 
P2=$!
echo Started consumer PID=$P2!

./run-consumer.sh $TOPIC $BROKER &
P3=$!
echo Started consumer PID=$P3!

sleep 30
echo Running producer...
./run-producer.sh $TOPIC $BROKER $LIMIT

echo Running waiting for consumers to die...
wait $P1 $P2 $P3
