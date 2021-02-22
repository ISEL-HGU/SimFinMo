# SimFinMo

Evaluates a project's prediction score based on the sorted distance calculations.
Outputs TP, FN, TN, FP, precision, recall, f1, MCC

## options: 
1. ##### topk
    a. description: calculates the average distance of buggy and clean within top 1000 instances to produce distance ratio (DR). Identifies an instance as buggy if DR < cutoff.
    b. example: <pre><code>./SimFinMo/ADP/bin/ADP topk 1000 rm_dups sqoop 0.001 0.9 1.1 > ./SimFinMo/out/sqoop_dups_avg.csv </pre></code>
2. ##### simple
    a. description: Identifies an instance as buggy if there is a buggy instance within top k distance.
    b. <pre><code>example: ./SimFinMo/ADP/bin/ADP simple rm_dups sqoop > ./SimFinMo/out/sqoop_dups_simple.csv </pre></code>
