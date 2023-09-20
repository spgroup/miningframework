#!/usr/bin/python

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
#     df.to_csv('resultTime-'+id_exec+'.csv', header=True, sep=';', mode='a', index=False, encoding='utf-8-sig')
#
#     dados_dict = {}
#
#     with open("./output/results/execution-"+id_exec+"/time.txt") as infile:
#         for actual_line in infile:
#             partes = actual_line.strip().split(';')
#             if len(partes) == 2:
#                 coluna, valor = partes[0].strip(), partes[1].strip().replace("s","")
#                 if coluna in dados_dict:
#                     dados_dict[coluna].append(valor)
#                 else:
#                     dados_dict[coluna] = [valor]
#
#     aux_dic = adjusting_dict(dados_dict)
#
#     final_dic = normalize_dict(aux_dic)
#
#     #save the last group
#     df = pd.DataFrame(final_dic)
#     df.to_csv('resultTime-'+id_exec+'.csv', sep=';', index=False, encoding='utf-8-sig')

# generating a CSV file of times for the Nth execution
for i in range(n):
    func.generating_sheets_result(str(i + 1), "./output/results/execution-", "time", "resultTime", ";")
