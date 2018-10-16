#!/usr/bin/env bash
set -o errexit
set -o nounset

curl "http://www-eu.apache.org/dist/kafka/1.1.0/kafka_2.12-1.1.0.tgz" -o ~/kafka.tgz
mkdir ~/kafka
cd ~/kafka
tar -xvzf ~/kafka.tgz --strip 1

