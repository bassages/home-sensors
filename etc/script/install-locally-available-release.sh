#!/bin/bash

if [[ ! "$1" ]]
then
  echo "[ERROR] Please specify the release to install as parameter. Example: home-sensors-v1.2.0.jar"
  exit 1
fi

release=releases/$1

if [[ ! -f $release ]]
then
  echo "[ERROR] Cannot find file ${release}. The following files are available:"
  ls -l releases
  exit 1
fi

echo "[INFO] Stopping service"
sudo service home-sensors stop

echo "[INFO] Updating symbolic link to ${release}"
ln -sf "${release}" home-sensors.jar

echo "[INFO] Wait 10 seconds until service is stopped"
sleep 10s

echo "[INFO] Starting service"
sudo service home-sensors start
