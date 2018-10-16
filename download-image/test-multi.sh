#!/usr/bin/env bash
set -o errexit
set -o nounset

BROKER=$1
TOPIC=urls18
LIMIT=20

echo Creating topics..
~/kafka/bin/kafka-topics.sh --create --zookeeper $BROKER:2181 --replication-factor 1 --partitions 6 --topic $TOPIC

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
./run-producer.sh $TOPIC $BROKER $LIMIT &
P4=$!
echo Started producer PID=$P4!

wait $P1 $P2 $P3 $P4
