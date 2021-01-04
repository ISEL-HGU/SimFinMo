#!/bin/bash

./ADP/bin/ADP ~/Desktop/$1/ $2 "0.01" "0.01" "1.0" "1" > ~/Desktop/$1/out/$2_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ $2 "0.001" "0.001" "1" "1" >> ~/Desktop/$1/out/$2_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ $2 "1" "5" "1000" "1" >> ~/Desktop/$1/out/$2_all.csv
#./ADP/bin/ADP ~/Desktop/$1/ $2 "1000" "5000" "10000000" "1" >> ~/Desktop/$1/out/$2_all.csv

echo "single "$1" complete!"
