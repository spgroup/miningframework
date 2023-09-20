#!/usr/bin/python
# generate a csv file with the messages from the conflicts found by scenario and analysis

import pandas as pd
import sys
import helper_functions as func

# Check if n was passed as a parameter, otherwise it will default to 10
n = 10
if len(sys.argv) > 1:
    n = int(sys.argv[1])
else:
    n = 10

# generating sheets with result time by id execution
# def generatingSheetResultTime(id_exec):
#     df = pd.DataFrame()
#     df.to_csv('conflicts_log-'+id_exec+'.csv', header=True, sep=';', mode='a', index=False, encoding='utf-8-sig')
#
#     aux_dictionary = {}
#
#     with open("./output/results/execution-"+id_exec+"/conflicts_log.txt") as infile:
#
#         for actual_line in infile:
#
#             parts = actual_line.strip().split('=>')
#
#             if len(parts) == 2:
#                 column, value = parts[0].strip(), parts[1].strip()
#
#                 if column in aux_dictionary:
#                     aux_dictionary[column].append(value)
#                 else:
#                     aux_dictionary[column] = [value]
#
#     final_dic = normalize_dict(adjusting_dict(aux_dictionary))
#
#     #save the last group
#     df = pd.DataFrame(final_dic)
#     df.to_csv('conflicts_log-'+id_exec+'.csv', sep=';', index=False, encoding='utf-8-sig')

# generating a CSV file of conflicts for the Nth execution
for i in range(n):
    func.generating_sheets_result(str(i + 1), "./output/results/execution-", "conflicts_log", "conflicts_log", "log =>")