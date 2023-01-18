#!/bin/bash
#cd into directory where this script is located
cd "$(dirname "$0")"
exec /opt/jdk-19.0.1/bin/java -jar home-sensors*.jar -Xmx128M
