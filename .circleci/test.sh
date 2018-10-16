#!/usr/bin/env bash
set -o errexit
set -o nounset

cd /src/project/download-image
bash ./test.sh kafka 3
