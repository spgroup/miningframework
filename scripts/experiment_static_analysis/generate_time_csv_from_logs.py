#!/usr/bin/python

import pandas as pd
import sys

# Check if n was passed as a parameter, otherwise it will default to 10
n = 10
if len(sys.argv) > 1:
    n = int(sys.argv[1])
else:
    n = 10

columnsFromExperiment = ["Config CF source", "Confluence source", "Config CF sink", "Confluence sink", "Config OA", "OA", "Config DFP left", "DFP left", "Config DFP right", "DFP right", "Config PDG left", "PDG left", "Config PDG right", "PDG right"]

mapColumnsFromLogs = {0: "Configure Soot Confluence 1", 1: "Time to perform Confluence 1",
                      2: "Configure Soot Confluence 2", 3: "Time to perform Confluence 2",
                      4: "Configure Soot OA Inter", 5: "Time to perform OA Inter",
                      6: "Configure Soot DFP", 7: "Time to perform DFP",
                      8: "Configure Soot DFP", 9: "Time to perform DFP",
                      10: "Configure Soot PDG", 11: "Time to perform PDG",
                      12: "Configure Soot PDG", 13: "Time to perform PDG"}

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
    if (pos == 6 and aux_list[pos] != "0"): #if is the first dfp occurrence
        result_pos = 8
    if (pos == 7 and aux_list[pos] != "0"): #if is the second dfp occurrence
        result_pos = 9
    if (pos == 10 and aux_list[pos] != "0"): #if is the first pdg occurrence
        result_pos = 12
    if (pos == 11 and aux_list[pos] != "0"): #if is the secont pdg occurrence
        result_pos = 13
    return result_pos

# generating sheets with result time by id execution
def generatingSheetResultTime(id_exec):
    df = pd.DataFrame(columns = columnsFromExperiment)
    df.to_csv('resultTime-'+id_exec+'.csv', header=True, sep=';', mode='a', index=False, encoding='utf-8-sig')

    with open("./output/results/execution-"+id_exec+"/time.txt") as infile:
        aux_list = ["0" for i in range(14)]
        count = 0
        for actual_line in infile:
            #if there is a complete group, save in file
            if count > 2 and "Configure Soot Confluence 1" in str(actual_line):
                df = pd.DataFrame([aux_list])
                df.to_csv('resultTime-'+id_exec+'.csv', header=False, sep=';', mode='a', index=False, encoding='utf-8-sig')
                aux_list = ["0" for i in range(14)]
                count = 0

            actual_value = [actual_line.replace(y, "").replace("s", "").replace(" ","") for y in values_columns if y in actual_line][0]
            actual_value = actual_value.replace(",", ".")

            aux_pos = getValue(mapColumnsFromLogs, str(actual_line))
            actual_position = updateIfOccurred(aux_pos, aux_list)

            aux_list[actual_position] = actual_value
            count = count + 1

    #save the last group
    df = pd.DataFrame([aux_list])
    df.to_csv('resultTime-'+id_exec+'.csv', header=False, sep=';', mode='a', index=False, encoding='utf-8-sig')

# generating csv times for n execution
for i in range(n):
    generatingSheetResultTime(str(i + 1))