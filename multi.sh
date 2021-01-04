#!/bin/bash

./ADP/bin/ADP ~/Desktop/$1/ "jena" "0.01" "0.01" "1.0" "$2" > ~/Desktop/$1/out/jena_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "jena" "0.001" "0.001" "1" "$2" >> ~/Desktop/$1/out/jena_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "jena" "1" "1" "1000" "$2" >> ~/Desktop/$1/out/jena_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "jena" "1000" "5000" "10000000" "$2" >> ~/Desktop/$1/out/jena_all.csv

./ADP/bin/ADP ~/Desktop/$1/ "maven" "0.01" "0.01" "1.0" "$2" > ~/Desktop/$1/out/maven_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "maven" "0.001" "0.001" "1" "$2" >> ~/Desktop/$1/out/maven_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "maven" "1" "1" "1000" "$2" >> ~/Desktop/$1/out/maven_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "maven" "1000" "5000" "10000000" "$2" >> ~/Desktop/$1/out/maven_all.csv

./ADP/bin/ADP ~/Desktop/$1/ "ranger" "0.01" "0.01" "1.0" "$2" > ~/Desktop/$1/out/ranger_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "ranger" "0.001" "0.001" "1" "$2" >> ~/Desktop/$1/out/ranger_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "ranger" "1" "1" "1000" "$2" >> ~/Desktop/$1/out/ranger_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "ranger" "1000" "5000" "10000000" "$2" >> ~/Desktop/$1/out/ranger_all.csv

./ADP/bin/ADP ~/Desktop/$1/ "sentry" "0.01" "0.01" "1.0" "$2" > ~/Desktop/$1/out/sentry_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "sentry" "0.001" "0.001" "1" "$2" >> ~/Desktop/$1/out/sentry_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "sentry" "1" "1" "1000" "$2" >> ~/Desktop/$1/out/sentry_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "sentry" "1000" "5000" "10000000" "$2" >> ~/Desktop/$1/out/sentry_all.csv

./ADP/bin/ADP ~/Desktop/$1/ "sqoop" "0.01" "0.01" "1.0" "$2" > ~/Desktop/$1/out/sqoop_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "sqoop" "0.001" "0.001" "1" "$2" >> ~/Desktop/$1/out/sqoop_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "sqoop" "1" "1" "1000" "$2" >> ~/Desktop/$1/out/sqoop_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "sqoop" "1000" "5000" "10000000" "$2" >> ~/Desktop/$1/out/sqoop_all.csv

./ADP/bin/ADP ~/Desktop/$1/ "syncope" "0.01" "0.01" "1.0" "$2" > ~/Desktop/$1/out/syncope_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "syncope" "0.001" "0.001" "1" "$2" >> ~/Desktop/$1/out/syncope_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "syncope" "1" "1" "1000" "$2" >> ~/Desktop/$1/out/syncope_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "syncope" "1000" "5000" "10000000" "$2" >> ~/Desktop/$1/out/syncope_all.csv

./ADP/bin/ADP ~/Desktop/$1/ "tez" "0.01" "0.01" "1.0" "$2" > ~/Desktop/$1/out/tez_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "tez" "0.001" "0.001" "1" "$2" >> ~/Desktop/$1/out/tez_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "tez" "1" "1" "1000" "$2" >> ~/Desktop/$1/out/tez_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ "tez" "1000" "5000" "10000000" "$2" >> ~/Desktop/$1/out/tez_all.csv

echo "multi "$1" with "$2"-top complete!"

