#!/usr/bin/env bash
set -o errexit
set -o nounset

TOPIC=$1
BROKER=$2
LIMIT=$3

java -jar target/DownloadImage-1.0-SNAPSHOT-jar-with-dependencies.jar producer $BROKER producer $TOPIC ../image-search/output.txt $LIMIT
