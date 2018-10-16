#!/usr/bin/env bash
set -o errexit
set -o nounset

export WORKFLOW_SERVICE_URL=https://ccenter.xcomponent.com/workflowsservice
export MONITORING_SERVICE_URL=https://ccenter.xcomponent.com/monitoringservice
export AUTH_SERVICE_URL=https://ccenter.xcomponent.com/authenticationservice

GENERATED_TOKEN=$(curl $AUTH_SERVICE_URL'/api/Authentication/User' \
                        -H 'Content-Type: application/json' \
                        --silent \
                        --data-binary '{
                            "username": "workerMeetup",
                            "password":"workerMeetup"
                        }' | jq --raw-output '.value')

echo token: $GENERATED_TOKEN

bash ./run.sh &
WORKERS_PID=$!

curl $WORKFLOW_SERVICE_URL'/api/start' \
    -H 'Authorization: Bearer '$GENERATED_TOKEN \
    -H 'Content-Type: application/json' \
    --verbose \
    --data-binary '{
        "WorkflowDefinitionId":"898d1430-cbdf-11e8-b7d3-a31a303f2ab5",
        "WorkflowDefinitionVersionNumber":15,
        "WorkflowName":"MeetupScenario",
        "InputParameters":{"terms":"test"}
    }'

WORKFLOWS_COUNT=0

while :; do
    WORKFLOWS_COUNT=$(curl $MONITORING_SERVICE_URL'/api/WorkspaceWorkflowInstances?workspaceName=DefaultWorkspace&workflowInstanceStatus=Running&workflowInstanceName=MeetupScenario' \
        --verbose \
        -H 'Authorization: Bearer '$GENERATED_TOKEN | jq --raw-output 'length')

    echo count: $WORKFLOWS_COUNT
    [ "$WORKFLOWS_COUNT" -gt 0 ] || break
    sleep 1
done

kill -9 $WORKERS_PID
