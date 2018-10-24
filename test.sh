#!/usr/bin/env bash
set -o errexit
set -o nounset

if [ -z "${RUN_LOCALHOST:-}" ]
then
    export WORKFLOW_SERVICE_URL=$KOORDINATOR_URL/workflowsservice
    export MONITORING_SERVICE_URL=$KOORDINATOR_URL/monitoringservice
    export AUTH_SERVICE_URL=$KOORDINATOR_URL/authenticationservice
else
    export KOORDINATOR_URL=http://localhost
    export WORKFLOW_SERVICE_URL=$KOORDINATOR_URL:7060
    export MONITORING_SERVICE_URL=$KOORDINATOR_URL:8079
    export AUTH_SERVICE_URL=$KOORDINATOR_URL:9009
fi

echo Generating token...
GENERATED_TOKEN=$(curl $AUTH_SERVICE_URL'/api/Authentication/User' \
                        -H 'Content-Type: application/json' \
                        --silent \
                        --data-binary '{
                            "username": "'$WORKER_USERNAME'",
                            "password":"'$WORKER_PASSWORD'"
                        }' | jq --raw-output '.value')

export WORKER_TOKEN=$GENERATED_TOKEN

echo Creating temporary scenario...
SCENARIO_ID=$(uuidgen)
sed "
s/\[SCENARIOID\]/$SCENARIO_ID/g
s#\[PWD\]#$PWD#g
s#\[BROKER\]#$KAFKA_BROKER#g
" < scenario.json > scenario.out.json

curl $WORKFLOW_SERVICE_URL'/api/save' \
    -H 'Authorization: Bearer '$GENERATED_TOKEN \
    -H 'Content-Type: application/json' \
    --silent \
    --data "@scenario.out.json" > /dev/null

export WORKFLOW_DEFINITION_NAME=$(cat scenario.out.json | jq --raw-output .name)
echo Scenario name: $WORKFLOW_DEFINITION_NAME

echo Starting workers...
bash ./run-workers.sh &
WORKERS_PID=$!

echo Starting scenario...
curl $WORKFLOW_SERVICE_URL'/api/start' \
    -H 'Authorization: Bearer '$GENERATED_TOKEN \
    -H 'Content-Type: application/json' \
    --silent \
    --data-binary '{
        "WorkflowDefinitionId":"'$SCENARIO_ID'",
        "WorkflowDefinitionVersionNumber": 0,
        "InputParameters":{"terms":"cars"}
    }'

timeout 30s bash <<"EOF"
    WORKFLOWS_COUNT=0

    while :; do
        echo Waiting for scenario to start...
        WORKFLOWS_COUNT=$(curl $MONITORING_SERVICE_URL'/api/WorkspaceWorkflowInstances?workspaceName=DefaultWorkspace&workflowInstanceStatus=Running&workflowInstanceName='$WORKFLOW_DEFINITION_NAME \
            --silent \
            -H 'Authorization: Bearer '$WORKER_TOKEN | jq --raw-output 'length')

        echo count: $WORKFLOWS_COUNT
        [ "$WORKFLOWS_COUNT" -gt 0 ] && break
        sleep 1
    done
EOF

timeout 2m bash <<"EOF"
    WORKFLOWS_COUNT=0

    while :; do
        echo Waiting for scenario to stop...
        WORKFLOWS_COUNT=$(curl $MONITORING_SERVICE_URL'/api/WorkspaceWorkflowInstances?workspaceName=DefaultWorkspace&workflowInstanceStatus=Running&workflowInstanceName='$WORKFLOW_DEFINITION_NAME \
            --silent \
            -H 'Authorization: Bearer '$WORKER_TOKEN | jq --raw-output 'length')

        echo count: $WORKFLOWS_COUNT
        [ "$WORKFLOWS_COUNT" -gt 0 ] || break
        sleep 1
    done
EOF

echo Deleting temporary scenario...

sed "s/\"isDeleted\": false/\"isDeleted\": true/g" < scenario.out.json > scenario.outDelete.json
curl $WORKFLOW_SERVICE_URL'/api/save' \
    -H 'Authorization: Bearer '$GENERATED_TOKEN \
    -H 'Content-Type: application/json' \
    --silent \
    --data "@scenario.outDelete.json" > /dev/null

echo Killing workers...
kill -9 $WORKERS_PID || true
