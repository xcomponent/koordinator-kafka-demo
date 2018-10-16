#!/usr/bin/env bash
set -o errexit
set -o nounset

echo Installing Kafka tools...
/src/project/install_kafka.sh

cd /src/project/download-image
bash ./test-multi.sh kafka 12 urls ~/kafka
