#!/usr/bin/env bash
set -o errexit
set -o nounset

echo Installing NodeJs...
curl -sL https://deb.nodesource.com/setup_10.x | sudo bash - && sudo apt install nodejs -y
cd /src/project/image-search
npm install 
cd ..

cd /src/project/
bash ./test.sh
