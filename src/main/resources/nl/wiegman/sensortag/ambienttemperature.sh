#!/bin/bash
SLEEPDURATION="$1"
while true
do
    # 0x29 must be set to a value of 01 to turn on the thermometer
    gatttool -b BC:6A:29:AC:7D:31 --char-write -a 0x29 -n 01

    # sleep a while to give the sensor time to obtain a value
    sleep 0.5

    # capture the thermometer value (as hex values)
    gatttool -b BC:6A:29:AC:7D:31 --char-read -a 0x25

    # 0x29 must be set to a value of 00 to turn on the thermometer
    gatttool -b BC:6A:29:AC:7D:31 --char-write -a 0x29 -n 00

    # wait for the next iteration
	sleep ${SLEEPDURATION};
done
