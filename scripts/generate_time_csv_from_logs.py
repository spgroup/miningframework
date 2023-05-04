#!/usr/bin/python

import pandas as pd
import sys

n = 10

if len(sys.argv) > 1:
    n = int(sys.argv[1])
else:
    n = 10

columns = ["Config CF source", "Confluence source", "Config CF sink", "Confluence sink", "Config OA", "OA", "Config DFP left", "DFP left", "Config DFP right", "DFP right", "Config PDG left", "PDG left", "Config PDG right", "PDG right"]

mapColumns = {0: "Configure Soot Confluence 1", 1: "Time to perform Confluence 1",
              2: "Configure Soot Confluence 2", 3: "Time to perform Confluence 2",
              4: "Configure Soot OA Inter", 5: "Time to perform OA Inter",
              6: "Configure Soot DFP", 7: "Time to perform DFP",
              8: "Configure Soot DFP", 9: "Time to perform DFP",
              10: "Configure Soot PDG", 11: "Time to perform PDG",
              12: "Configure Soot PDG", 13: "Time to perform PDG"}
values_columns = []
for (k, v) in mapColumns.items():
    values_columns.append(v)

def getValue(mapCols, value):
    for (k, v) in mapCols.items():
        if (v in value):
            return k

def generating(id_exec):
    df = pd.DataFrame(columns = columns)
    df.to_csv('resultTime-'+id_exec+'.csv', header=True, sep=';', mode='a', index=False, encoding='utf-8-sig')

    with open("./output/results/execution-"+id_exec+"/time.txt") as infile:
        aux_list = ["0" for i in range(14)]
        cont = 0
        for i in infile:
            #if there is a complete group, save in file
            if cont > 2 and "Configure Soot Confluence 1" in str(i):
                df = pd.DataFrame([aux_list])
                df.to_csv('resultTime-'+id_exec+'.csv', header=False, sep=';', mode='a', index=False, encoding='utf-8-sig')
                aux_list = ["0" for i in range(14)]
                cont = 0

            actual_value = [i.replace(y, "").replace("s", "").replace(" ","") for y in values_columns if y in i][0]
            actual_value = actual_value.replace(",", ".")

            pos = getValue(mapColumns, str(i))

            if (pos == 6 and aux_list[pos] != "0"): #if is the first dfp occurrence
                pos = 8
            if (pos == 7 and aux_list[pos] != "0"): #if is the second dfp occurrence
                pos = 9

            if (pos == 10 and aux_list[pos] != "0"): #if is the first pdg occurrence
                pos = 12
            if (pos == 11 and aux_list[pos] != "0"): #if is the secont pdg occurrence
                pos = 13

            aux_list[pos] = actual_value
            cont = cont + 1

    #save the last group
    df = pd.DataFrame([aux_list])
    df.to_csv('resultTime-'+id_exec+'.csv', header=False, sep=';', mode='a', index=False, encoding='utf-8-sig')

# Generating csv times for n execution
for i in range(n):
    generating(str(i+1))