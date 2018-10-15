#!/bin/bash

TOPIC=$1

mkdir images/
java -jar target/DownloadImage-1.0-SNAPSHOT-jar-with-dependencies.jar consumer url $TOPIC images/
