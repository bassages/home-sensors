#!/bin/bash
#cd into directory where this script is located
cd "$(dirname "$0")"
exec java -jar home-smart-meter-*.jar -Xmx128M
