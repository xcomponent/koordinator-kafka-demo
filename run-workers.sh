#!/usr/bin/env bash
set -o errexit
set -o nounset

export TASK_CATALOG_URL=$KOORDINATOR_URL/taskcatalogservice
export TASK_STATUS_URL=$KOORDINATOR_URL/taskstatusservice
export TASK_POLLING_URL=$KOORDINATOR_URL/pollingservice

cd image-search
bash ./run.sh &
P1=$!
echo Started image search worker PID=$P1!
cd ..

cd download-image
bash ./run-consumer-worker.sh &
P2=$!
echo Started consumer worker PID=$P2!

bash ./run-producer-worker.sh &
P3=$!
echo Started producer worker PID=$P3!
cd ..

echo Press ENTER to stop
read

kill -9 $P1 || true
kill -9 $P2 || true
kill -9 $P3 || true
