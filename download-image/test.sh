#!/usr/bin/env bash
set -o errexit
set -o nounset

BROKER=$1
TOPIC=urls
LIMIT=$2

echo Running consumer...
bash ./run-consumer.sh $TOPIC $BROKER & 
P1=$!
echo Started consumer PID=$P1!

sleep 30
echo Running producer...
bash ./run-producer.sh $TOPIC $BROKER $LIMIT &
P2=$!
echo Started producer PID=$P2!

wait $P1 $P2
