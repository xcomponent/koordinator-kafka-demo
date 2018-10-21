#!/usr/bin/env bash
set -o errexit
set -o nounset

echo Installing task in the catalog...
curl -X POST \
  $TASK_CATALOG_URL/api/TaskCatalog \
  -H 'accept: application/json' \
  -H 'authorization: '$WORKER_TOKEN \
  -H 'content-type: application/json' \
  --silent \
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
