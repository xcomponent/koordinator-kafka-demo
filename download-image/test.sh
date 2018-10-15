#!/bin/bash

docker run -d --rm -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=localhost --env ADVERTISED_PORT=9092 --env AUTO_CREATE_TOPICS=true spotify/kafka
echo Waiting...
sleep 30
echo Running consumer...
./run-consumer.sh urls12 &

echo Running producer...
./run-producer.sh urls12 &
