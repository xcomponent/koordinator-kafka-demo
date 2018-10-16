#!/usr/bin/env bash
set -o errexit
set -o nounset

TOPIC=$1
BROKER=$2

mkdir images/ || true
java -jar target/DownloadImage-1.0-SNAPSHOT-jar-with-dependencies.jar consumer $BROKER url $TOPIC images/
