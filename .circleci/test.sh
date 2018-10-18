#!/usr/bin/env bash
set -o errexit
set -o nounset

echo Installing NodeJs...
curl -sL https://deb.nodesource.com/setup_10.x | sudo bash - && sudo apt install nodejs -y
cd /src/project/image-search
npm install 
cd ..

echo Installing uuidgen...
sudo apt install uuid-runtime -y

cd /src/project/
bash ./test.sh
