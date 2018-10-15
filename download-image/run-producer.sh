#!/bin/bash

TOPIC=$1
BROKER=$2

java -jar target/DownloadImage-1.0-SNAPSHOT-jar-with-dependencies.jar producer $BROKER producer $TOPIC ../image-search/output.txt
