#!/usr/bin/env bash
set -o errexit
set -o nounset

echo Installing Kafka tools...
curl "http://www-eu.apache.org/dist/kafka/1.1.0/kafka_2.12-1.1.0.tgz" -o ~/kafka.tgz
mkdir ~/kafka
cd ~/kafka
tar -xvzf ~/kafka.tgz --strip 1

cd /src/project/download-image
bash ./test-multi.sh kafka 12 urls ~/kafka
