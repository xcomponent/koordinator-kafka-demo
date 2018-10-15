#!/bin/bash

TOPIC=urls
KAFKA=$(docker run -d --rm -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=localhost --env ADVERTISED_PORT=9092 --env AUTO_CREATE_TOPICS=true spotify/kafka)$

echo Kafka container $KAFKA started...
sleep 30

echo Creating topics..
~/kafka/bin/kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --replication-factor 1 --partitions 6 --topic $TOPIC

echo Running consumers...
./run-consumer.sh $TOPIC & 
P1=$!
echo Started consumer PID=$P1!

./run-consumer.sh $TOPIC & 
P2=$!
echo Started consumer PID=$P2!

./run-consumer.sh $TOPIC & 
P3=$!
echo Started consumer PID=$P3!

sleep 30
echo Running producer...
./run-producer.sh $TOPIC &
P4=$!
echo Started producer PID=$P4!

wait $P1 $P2 $P3 $P4

docker stop $KAFKA
