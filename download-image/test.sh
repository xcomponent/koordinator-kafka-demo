#!/bin/bash

BROKER=$1
TOPIC=urls

echo Running consumer...
./run-consumer.sh $TOPIC $BROKER & 
P1=$!
echo Started consumer PID=$P1!

sleep 30
echo Running producer...
./run-producer.sh $TOPIC $BROKER &
P2=$!
echo Started producer PID=$P2!

wait $P1 $P2
