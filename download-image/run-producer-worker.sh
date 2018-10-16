#!/usr/bin/env bash
set -o errexit
set -o nounset

export WORKER_TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoiV29ya2VyIiwiaXNzIjoiQXV0aGVudGljYXRpb25TZXJ2aWNlIiwidXNlciI6Indvcmtlck1lZXR1cCIsImluZm8iOiJiODU5ZTdiZTExMWFmMDRiOTA3MzkyMDlhOGY0MGU0NSJ9.3yMZw2wVWpiysbB2maTgQW5qry3o6pfgtl1LHlC_yx4
export TASK_CATALOG_URL=https://ccenter.xcomponent.com/taskcatalogservice
export TASK_STATUS_URL=https://ccenter.xcomponent.com/taskstatusservice
export TASK_POLLING_URL=https://ccenter.xcomponent.com/pollingservice

echo Installing task in the catalog...
curl -X POST \
  $TASK_CATALOG_URL/api/TaskCatalog \
  -H 'accept: application/json' \
  -H 'authorization: '$WORKER_TOKEN \
  -H 'content-type: application/json' \
  --verbose \
  -d '{
   "catalogTaskDefinitions": [ 
     {
       "namespace": "Meetup",
       "name": "UrlsProducer",
       "inputs": [ 
           {
            "name": "broker"
           },
           {
            "name": "clientId"
           },
           {
            "name": "output-topic"
           },
           {
            "name": "input-file"
           },
           {
            "name": "limit"
           }
        ],
       "schemaVersion": 0
     } 
   ]
 }'

echo Running worker... 
java -jar target/DownloadImage-1.0-SNAPSHOT-jar-with-dependencies.jar producerWorker
