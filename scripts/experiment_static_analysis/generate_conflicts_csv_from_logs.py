#!/usr/bin/python

import pandas as pd
import sys

# Check if n was passed as a parameter, otherwise it will default to 10
n = 10
if len(sys.argv) > 1:
    n = int(sys.argv[1])
else:
    n = 10

columnsFromExperiment = ["Confluence", "OA", "DFP left", "DFP right", "PDG left", "PDG right"]

mapColumnsFromLogs = {0: "Confluence", 1: "OA",
                      2: "DFP", 3: "DFP",
                      4: "PDG", 5: "PDG"}

values_columns = []
for (key, value) in mapColumnsFromLogs.items():
    values_columns.append(value)

def getValue(mapCols, value):
    for (key, val) in mapCols.items():
        if (val in value):
            return key

# there are columns with repeated names, we check if the first one has already been filled and
# return the value of the next one to be filled
def updateIfOccurred(pos, aux_list):
    result_pos = pos
    if (pos == 2 and aux_list[pos] != "0"): #if is the first dfp occurrence
        result_pos = 3
    if (pos == 4 and aux_list[pos] != "0"): #if is the secont pdg occurrence
        result_pos = 5
    return result_pos

# generating sheets with result time by id execution
def generatingSheetResultTime(id_exec):
    df = pd.DataFrame(columns = columnsFromExperiment)
    df.to_csv('conflicts_log-'+id_exec+'.csv', header=True, sep=';', mode='a', index=False, encoding='utf-8-sig')

    with open("./output/results/execution-"+id_exec+"/conflicts_log.txt") as infile:
        aux_list = ["0" for i in range(6)]
        actual_position = 0

        for actual_line in infile:
            #if there is a complete group, save in file
            if actual_position == 5:
                df = pd.DataFrame([aux_list])
                df.to_csv('conflicts_log-'+id_exec+'.csv', header=False, sep=';', mode='a', index=False, encoding='utf-8-sig')
                aux_list = ["0" for i in range(6)]

            actual_line_split = actual_line.split(" [")
            actual_analysis = actual_line_split[0]
            actual_log = actual_line_split[1]

            # print(actual_line_split)
            aux_pos = getValue(mapColumnsFromLogs, str(actual_analysis))
            actual_position = updateIfOccurred(aux_pos, aux_list)

            if "]" in actual_log:
                aux_list[actual_position] = "[]"
            else:
                aux_list[actual_position] = actual_log

    #save the last group
    df = pd.DataFrame([aux_list])
    df.to_csv('conflicts_log-'+id_exec+'.csv', header=False, sep=';', mode='a', index=False, encoding='utf-8-sig')

# generating a CSV file of conflicts for the Nth execution
for i in range(n):
    generatingSheetResultTime(str(i + 1))