#!/bin/bash

./ADP/bin/ADP ~/Desktop/$1/ "maven" "1" "1" "1" "1" > ~/Desktop/$1/tps/maven.csv
./ADP/bin/ADP ~/Desktop/$1/ "ranger" "1" "1" "1" "1" > ~/Desktop/$1/tps/ranger.csv
./ADP/bin/ADP ~/Desktop/$1/ "sentry" "1" "1" "1" "1" > ~/Desktop/$1/tps/sentry.csv
./ADP/bin/ADP ~/Desktop/$1/ "sqoop" "1" "1" "1" "1" > ~/Desktop/$1/tps/sqoop.csv
./ADP/bin/ADP ~/Desktop/$1/ "syncope" "1" "1" "1" "1" > ~/Desktop/$1/tps/syncope.csv
./ADP/bin/ADP ~/Desktop/$1/ "tez" "1" "1" "1" "1" > ~/Desktop/$1/tps/tez.csv


echo tp_getter for $1  complete!

