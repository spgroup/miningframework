#!/usr/bin/python
# generate a csv file with the messages from the conflicts found by scenario and analysis

import pandas as pd
import sys

# Check if n was passed as a parameter, otherwise it will default to 10
n = 10
if len(sys.argv) > 1:
    n = int(sys.argv[1])
else:
    n = 10

def menor_tamanho_chave(dicionario):
    menor_chave = None
    menor_tamanho = float('inf')  # Inicialmente, consideramos o tamanho infinito

    for chave, valor in dicionario.items():
        if isinstance(valor, list) and len(valor) < menor_tamanho:
            menor_chave = chave
            menor_tamanho = len(valor)

    return menor_tamanho
def chaves_com_tamanho_n(dicionario, tamanho):
    chaves = []
    for chave, valor in dicionario.items():
        if isinstance(valor, list) and len(valor) > tamanho:
            chaves.append(chave)
    return chaves

def adjusting_dict(dados_dict):
    # Chave que você deseja dividir
    dicionario = dados_dict

    len_dic = menor_tamanho_chave(dados_dict)

    chaves = chaves_com_tamanho_n(dados_dict, len_dic)

    for chave in chaves:
        # Verifica se a chave existe no dicionário
        if chave in dicionario:
            # Obtém a lista de valores correspondente à chave
            lista_valores = dicionario[chave]

            # Verifica se a lista tem mais de len_dic elementos
            if len(lista_valores) > len_dic:

                # Divide a lista em duas partes
                primeira_parte = lista_valores[::2]
                segunda_parte = lista_valores[1::2]

                # Cria duas novas chaves no dicionário com as partes divididas
                dicionario[chave + ' left-right'] = primeira_parte
                dicionario[chave + ' right-left'] = segunda_parte

                # Exclui a chave antiga
                del dicionario[chave]
            else:
                dicionario[chave] = lista_valores


    # Exibe o dicionário atualizado
    return dicionario


def normalize_dict(dic):
    # Encontre o tamanho máximo entre todas as listas
    tamanho_maximo = max(len(valor) for valor in dic.values())

    # Percorra as chaves do dicionário
    for chave, valor in dic.items():
        if isinstance(valor, list):
            # Se a lista for menor que o tamanho máximo, preencha com zeros
            while len(valor) < tamanho_maximo:
                valor.append(0)

    return dic

# generating sheets with result time by id execution
def generatingSheetResultTime(id_exec):
    df = pd.DataFrame()
    df.to_csv('conflicts_log-'+id_exec+'.csv', header=True, sep=';', mode='a', index=False, encoding='utf-8-sig')

    dados_dict = {}

    with open("./output/results/execution-"+id_exec+"/visited_methods.txt") as infile:

        for actual_line in infile:

            partes = actual_line.strip().split(';')

            if len(partes) == 2:
                coluna, valor = partes[0].strip(), partes[1].strip()

                if coluna in dados_dict:
                    dados_dict[coluna].append(valor)
                else:
                    dados_dict[coluna] = [valor]

    aux_dic = adjusting_dict(dados_dict)

    final_dic = normalize_dict(aux_dic)

    #save the last group
    df = pd.DataFrame(final_dic)
    df.to_csv('visited_methods-'+id_exec+'.csv', sep=';', index=False, encoding='utf-8-sig')

# generating a CSV file of conflicts for the Nth execution
for i in range(n):
    generatingSheetResultTime(str(i + 1))