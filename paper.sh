#!/bin/bash

./ADP/bin/ADP ~/Desktop/$1/ "jena" "0.000001" "0.1" "1" "2" > ~/Desktop/$1/out/jena.csv
./ADP/bin/ADP ~/Desktop/$1/ "jena" "1" "1" "10" "2" >> ~/Desktop/$1/out/jena.csv

./ADP/bin/ADP ~/Desktop/$1/ "maven" "0.000001" "0.1" "1" "2" > ~/Desktop/$1/out/maven.csv
./ADP/bin/ADP ~/Desktop/$1/ "maven" "1" "1" "10" "2" >> ~/Desktop/$1/out/maven.csv

./ADP/bin/ADP ~/Desktop/$1/ "ranger" "0.000001" "0.1" "1" "2" > ~/Desktop/$1/out/ranger.csv
./ADP/bin/ADP ~/Desktop/$1/ "ranger" "1" "1" "10" "2" >> ~/Desktop/$1/out/ranger.csv

./ADP/bin/ADP ~/Desktop/$1/ "sentry" "0.000001" "0.1" "1" "2" > ~/Desktop/$1/out/sentry.csv
./ADP/bin/ADP ~/Desktop/$1/ "sentry" "1" "1" "10" "2" >> ~/Desktop/$1/out/sentry.csv

./ADP/bin/ADP ~/Desktop/$1/ "sqoop" "0.000001" "0.1" "1" "2" > ~/Desktop/$1/out/sqoop.csv
./ADP/bin/ADP ~/Desktop/$1/ "sqoop" "1" "1" "10" "2" >> ~/Desktop/$1/out/sqoop.csv

./ADP/bin/ADP ~/Desktop/$1/ "syncope" "0.000001" "0.1" "1" "2" > ~/Desktop/$1/out/syncope.csv
./ADP/bin/ADP ~/Desktop/$1/ "syncope" "1" "1" "10" "2" >> ~/Desktop/$1/out/syncope.csv

./ADP/bin/ADP ~/Desktop/$1/ "tez" "0.000001" "0.1" "1" "2" > ~/Desktop/$1/out/tez.csv
./ADP/bin/ADP ~/Desktop/$1/ "tez" "1" "1" "10" "2" >> ~/Desktop/$1/out/tez.csv