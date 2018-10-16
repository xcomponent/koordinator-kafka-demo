#!/usr/bin/env bash
set -o errexit
set -o nounset

cd /src/project/download-image
bash ./test-multi.sh kafka 12 urls
