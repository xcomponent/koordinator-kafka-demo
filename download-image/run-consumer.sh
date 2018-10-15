#!/bin/bash

TOPIC=$1
BROKER=$2

mkdir images/
java -jar target/DownloadImage-1.0-SNAPSHOT-jar-with-dependencies.jar consumer $BROKER url $TOPIC images/
