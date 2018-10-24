#!/usr/bin/env bash
set -o errexit
set -o nounset

if [ -z "${RUN_LOCALHOST:-}" ]
then
    export TASK_CATALOG_URL=$KOORDINATOR_URL/taskcatalogservice
    export TASK_STATUS_URL=$KOORDINATOR_URL/taskstatusservice
    export TASK_POLLING_URL=$KOORDINATOR_URL/pollingservice
    export UPLOAD_URL=$KOORDINATOR_URL/uploadfileservice
else
    export KOORDINATOR_URL=http://localhost
    export TASK_CATALOG_URL=$KOORDINATOR_URL:8099
    export TASK_STATUS_URL=$KOORDINATOR_URL:9999
    export TASK_POLLING_URL=$KOORDINATOR_URL:7000
    export UPLOAD_URL=$KOORDINATOR_URL:7099
fi

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

bash ./run-zipimages.sh &
P4=$!
echo Started zip images worker PID=$P4!
cd ..

wait $P1 $P2 $P3 $P4
