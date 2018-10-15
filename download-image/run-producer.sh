#!/bin/bash

TOPIC=$1

java -jar target/DownloadImage-1.0-SNAPSHOT-jar-with-dependencies.jar producer producer $TOPIC ../image-search/output.txt
