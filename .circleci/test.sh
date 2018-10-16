#!/usr/bin/env bash
set -o errexit
set -o nounset

echo Installing Kafka tools...
cd /src/project/
bash ./test.sh
