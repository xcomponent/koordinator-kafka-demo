#!/usr/bin/env bash
set -o errexit
set -o nounset

export WORKER_TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoiV29ya2VyIiwiaXNzIjoiQXV0aGVudGljYXRpb25TZXJ2aWNlIiwidXNlciI6Indvcmtlck1lZXR1cCIsImluZm8iOiJiODU5ZTdiZTExMWFmMDRiOTA3MzkyMDlhOGY0MGU0NSJ9.3yMZw2wVWpiysbB2maTgQW5qry3o6pfgtl1LHlC_yx4
export TASK_CATALOG_URL=https://ccenter.xcomponent.com/taskcatalogservice
export TASK_STATUS_URL=https://ccenter.xcomponent.com/taskstatusservice
export TASK_POLLING_URL=https://ccenter.xcomponent.com/pollingservice

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
