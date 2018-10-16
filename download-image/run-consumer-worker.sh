#!/usr/bin/env bash
set -o errexit
set -o nounset

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
       "name": "UrlsConsumer",
       "inputs": [ 
           {
            "name": "broker"
           },
           {
            "name": "groupId"
           },
           {
            "name": "input-topic"
           },
           {
            "name": "output-dir"
           }
        ],
       "schemaVersion": 0
     } 
   ]
 }'

echo Running worker... 
mkdir images/ || true > /dev/null
java -jar target/DownloadImage-1.0-SNAPSHOT-jar-with-dependencies.jar consumerWorker
