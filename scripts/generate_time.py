#!/usr/bin/python

import pandas as pd
import os
import numpy as np
import seaborn as sns
import datetime
import chardet
import time
from pandas.api.types import is_number
from matplotlib import pyplot as plt
import dask.dataframe as dd
import datetime
import sys

n = 10

if len(sys.argv) > 1:
    n = int(sys.argv[1])
else:
    n = 10

colunas = ["Config CF source", "Confluence source", "Config CF sink", "Confluence sink", "Config OA", "OA", "Config DFP left", "DFP left", "Config DFP right", "DFP right", "Config PDG left", "PDG left", "Config PDG right", "PDG right"]

listColunas = {0: "Configure Soot Confluence 1", 1: "Time to perform Confluence 1",
               2: "Configure Soot Confluence 2", 3: "Time to perform Confluence 2",
               4: "Configure Soot OA Inter", 5: "Time to perform OA Inter",
               6: "Configure Soot DFP", 7: "Time to perform DFP",
               8: "Configure Soot DFP", 9: "Time to perform DFP",
               10: "Configure Soot PDG", 11: "Time to perform PDG",
               12: "Configure Soot PDG", 13: "Time to perform PDG"}
values_colunas = []
for (k, v) in listColunas.items():
    
    values_colunas.append(v)

def getValue(listColunas, value):
    for (k, v) in listColunas.items():
        if (v in value):
            return k


def generating(id_exec):
    df = pd.DataFrame(columns = colunas)
    df.to_csv('resultTime-'+id_exec+'.csv', header=True, sep=';', mode='a', index=False, encoding='utf-8-sig')

    listColunas = {0: "Configure Soot Confluence 1", 1: "Time to perform Confluence 1",
                2: "Configure Soot Confluence 2", 3: "Time to perform Confluence 2",
                4: "Configure Soot OA Inter", 5: "Time to perform OA Inter",
                6: "Configure Soot DFP", 7: "Time to perform DFP",
                8: "Configure Soot DFP", 9: "Time to perform DFP",
                10: "Configure Soot PDG", 11: "Time to perform PDG",
                12: "Configure Soot PDG", 13: "Time to perform PDG"}

    cont = 0
    aux = []
    with open("./output/results/execution-"+id_exec+"/time.txt") as infile:
        for i in infile:
            finish = False
            actual_value = [i.replace(y, "").replace("s", "").replace(" ","") for y in values_colunas if y in i][0]
            actual_value = actual_value.replace(",", ".")
            if (str(listColunas.get(cont)) in str(i)):
                aux.append(actual_value)
            else:
                pos = getValue(listColunas, str(i))
                while (cont < pos):
                    cont = cont + 1
                    aux.append("0")
                    if (cont%14)+1 == 1:
                        cont = 0
                aux.append(actual_value)
                
            cont = cont + 1
    
            if (cont%14)+1 == 1:
                df = pd.DataFrame([aux])
                df.to_csv('resultTime-'+id_exec+'.csv', header=False, sep=';', mode='a', index=False, encoding='utf-8-sig')
                aux = []
                cont = 0
                finish = True
    if not finish:
        df = pd.DataFrame([aux])
        df.to_csv('resultTimeFinal.csv', header=False, sep=';', mode='a', index=False, encoding='utf-8-sig')
    
# Generating time of analysis
for i in range(n):
    generating(str(i+1))