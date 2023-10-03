import pandas as pd
import os
import numpy as np
import datetime
import chardet
import time
from matplotlib import pyplot as plt
from collections import Counter
from itertools import combinations

soot_results = pd.read_csv('../miningframework/output/results/execution-1/soot-results.csv', sep=';', encoding='latin-1', on_bad_lines='skip', low_memory=False)
loi = pd.read_csv('../miningframework/input/LOI.csv', sep=';', encoding='latin-1', on_bad_lines='skip', low_memory=False)


def get_loi(project, class_name,  method, merge_commit):

        filter_scenario = (loi['Project'] == str(project)) & (loi['Merge Commit'] == str(merge_commit)) & (loi['Class Name'] == str(class_name)) & (loi['Method or field declaration changed by the two merged branches'] == str(method))
        value_LOI = ""

        if filter_scenario.any():
            value_LOI = loi.loc[filter_scenario, 'Locally Observable Interference'].values[0]

        return value_LOI


def get_name_analysis(list_name):
    names = []
    for i in list_name:
        if (i in left_right_analysis):
            names.append("left right "+i)
            names.append("right left "+i)
        else:
            names.append(i)
    return names

def get_reverse_name(lists):
    names = []
    for elem_list in lists:
        aux_list = []
        for i in elem_list:
            if "left right" in i:
                aux_list.append(i.replace("left right ", ""))
            elif ("right left " not in i):
                aux_list.append(i)
        names.append(aux_list)
    return names

def calculate_matrix(columns):
    results = []
    for index, row in soot_results.iterrows():
        values = [row[column] for column in columns]
        actual_loi = get_loi(row['project'], row['class'], row['method'], row['merge commit'])
        or_value = any(value != 'false' for value in values)
        result = ""
        # print("OR:", or_value, "LOI:", actual_loi)
        if or_value == True and actual_loi == 'Yes':
            result = "TRUE POSITIVE"
        elif or_value == False and actual_loi == 'No':
            result = "TRUE NEGATIVE"
        elif or_value == False and actual_loi == 'Yes':
            result = "FALSE NEGATIVE"
        elif or_value == True and actual_loi == 'No':
            result = "FALSE POSITIVE"
        if actual_loi != "-":
            results.append(result)
    return results

def count_fp_fn(list_result):
    # Criar um contador dos elementos da lista
    element_count = Counter(list_result)

    result = []
    # Imprimir a contagem de elementos repetidos
    for element, count in element_count.items():
        if count > 1:
            result.append((str(element)+": "+str(count)))
    return result

class Longest:
    maiorPrecision = -1.0
    maiorRecall = -1.0
    maiorF1 = -1.0
    maiorAcuracia = -1.0
    mPrecision = []
    mRecall = []
    mF1 = []
    mAcuracia = []
    mPrecision_t = []
    mRecall_t = []
    mF1_t = []
    mAcuracia_t = []

    def __init__(self):
        self.maiorPrecision = -1.0
        self.maiorRecall = -1.0
        self.maiorF1 = -1.0
        self.maiorAcuracia = -1.0
        mPrecision = []
        mRecall = []
        mF1 = []
        mAcuracia = []
        mPrecision_t = []
        mRecall_t = []
        mF1_t = []
        mAcuracia_t = []


    def confusion_matrix(self, options, values_elem):

        # Inicializar as variáveis
        tp = 0
        fp = 0
        tn = 0
        fn = 0
        
        # Extrair os valores dos elementos da lista
        for option in options:
            if "TRUE POSITIVE" in option:
                tp = int(option.split(': ')[1])
            elif "FALSE POSITIVE" in option:
                fp = int(option.split(': ')[1])
            elif "TRUE NEGATIVE" in option:
                tn = int(option.split(': ')[1])
            elif "FALSE NEGATIVE" in option:
                fn = int(option.split(': ')[1])
       
        # Calcular as métricas se todos os valores foram extraídos
        if tp is not None and fp is not None and tn is not None and fn is not None:
            if tp == 0 and fp == 0:
                precision = '-'
            else:
                precision = tp / (tp + fp)

            if tp == 0 and fn == 0:
                recall = '-'
            else:
                recall = tp / (tp + fn)

            if (precision == 0 and recall == 0) or (precision == '-' and recall == '-'):
                f1_score = '-'
            else:
                f1_score = 2 * (precision * recall) / (precision + recall)

            if (tp == 0 and tn == 0 and fp == 0 and fn == 0) or (tp == '-' and tn == '-' and fp == '-' and fn == '-'):
                accuracy = '-'
            else:
                accuracy = (tp + tn) / (tp + tn + fp + fn)

            if (precision != '-' and precision > self.maiorPrecision):
                self.maiorPrecision = precision
                self.mPrecision = []
                self.mPrecision.append(values_elem)

            if (precision != '-' and precision == self.maiorPrecision):
                if values_elem not in self.mPrecision:
                    self.mPrecision.append(values_elem)

            if (recall != '-' and recall > self.maiorRecall):
                self.maiorRecall = recall
                self.mRecall = []
                self.mRecall.append(values_elem)

            if (recall != '-' and recall == self.maiorRecall):
                if values_elem not in self.mRecall:
                    self.mRecall.append(values_elem)


            if (f1_score != '-' and f1_score > self.maiorF1):
                self.maiorF1 = f1_score
                self.mF1 = []
                self.mF1.append(values_elem)

            if (f1_score != '-' and f1_score == self.maiorF1):
                if values_elem not in self.mF1:
                    self.mF1.append(values_elem)

            if (accuracy != '-' and accuracy > self.maiorAcuracia):
                self.maiorAcuracia = accuracy
                self.mAcuracia = []
                self.mAcuracia.append(values_elem)

            if (accuracy != '-' and accuracy == self.maiorAcuracia):
                if values_elem not in self.mAcuracia:
                    self.mAcuracia.append(values_elem)

            # Imprimir as métricas

            print(f"Precision: {precision:.2f}" if isinstance(precision, (int, float)) else f"Accuracy: {precision}")
            print(f"Recall: {recall:.2f}" if isinstance(recall, (int, float)) else f"Accuracy: {recall}")
            print(f"F1 Score: {f1_score:.2f}" if isinstance(f1_score, (int, float)) else f"Accuracy: {f1_score}")
            print(f"Accuracy: {accuracy:.2f}" if isinstance(accuracy, (int, float)) else f"Accuracy: {accuracy}")

            result_metrics = {
                "precision": precision,
                "recall": recall,
                "f1_score": f1_score,
                "accuracy": accuracy
            }
            return result_metrics
        else:
            print("It was not possible to extract all the necessary values: ", tp, fp, tn, fn)

        return None

def sum_n_esimo_element(lista_de_listas, n):
    return sum(float(str(sublista[n]).replace(",", ".")) for sublista in lista_de_listas)

def get_sum_all_list(all_list):
    return [sum_n_esimo_element(all_list, i) for i in range(len(all_list[0]))]

def get_total_time(list_analysis):
    result = []
    for column_analysis in list_analysis:
        actual_time = [i for i in df_t[column_analysis]]
        result.append(actual_time)
    return result

def get_mean(values):
    return pd.Series(get_sum_all_list(values)).mean()

def get_median(values):
    return pd.Series(get_sum_all_list(values)).median()

def get_sum(values):
    return pd.Series(get_sum_all_list(values)).sum()

def get_standard_desviation(values):
    return pd.Series(get_sum_all_list(values)).std()

def get_mean_metric(metric, config):
    metric_t = []
    for i in metric:
        aux_list = get_name_analysis_time(i, config)
        result = get_total_time(aux_list)
        value = round(pd.Series(get_mean(result)).sum(), 2)
        metric_t.append(value)
    return metric_t

def get_sum_metric(metric, config):
    metric_t = []
    for i in metric:
        aux_list = get_name_analysis_time(i, config)
        result = get_total_time(aux_list)
        value = round(pd.Series(get_sum(result)).sum(), 2)
        metric_t.append(value)
    return metric_t

def get_median_metric(metric, config):
    metric_t = []
    for i in metric:
        aux_list = get_name_analysis_time(i, config)
        result = get_total_time(aux_list)
        value = round(pd.Series(get_median(result)).sum(), 2)
        metric_t.append(value)
    return metric_t

def get_std_metric(metric, config):
    metric_t = []
    for i in metric:
        aux_list = get_name_analysis_time(i, config)
        result = get_total_time(aux_list)
        value = round(pd.Series(get_standard_desviation(result)).sum(), 2)
        metric_t.append(value)
    return metric_t

def get_name_analysis_time(list_name, config):
    names = []
    for i in list_name:
        if ("Confluence" in i):
            actual_name = i.replace("Confluence ", "Confluence 1 ")
            if config:
                names.append("Configure Soot "+actual_name)
            names.append("Time to perform "+actual_name)

            actual_name = i.replace("Confluence ", "Confluence 2 ")

            if config:
                names.append("Configure Soot "+actual_name)

            names.append("Time to perform "+actual_name)

        elif ("left right" in i):
            actual_name = i.replace("left right ", "")
            if ("DF" in i):
                actual_name = actual_name.replace("-", " ")
            else:
                actual_name = actual_name.replace("-", "")

            if config:
                names.append("Configure Soot "+actual_name+" left-right")
                names.append("Configure Soot "+actual_name+" right-left")
            names.append("Time to perform "+actual_name+" left-right")
            names.append("Time to perform "+actual_name+" right-left")
        elif ("right left" not in str(i)):
            if config:
                names.append("Configure Soot "+i)
            names.append("Time to perform "+i)

    return names

def remove_nested_best(best_list) :
    return [i for i in best_list if not any(all(item in i for item in j) for j in best_list if i != j)]

def to_string_as_set(best_list):
    tranformed_list_in_set = [set(sub_list) for sub_list in best_list]
    result = ' '.join(map(str, tranformed_list_in_set))
    return result

def convert_list_to_tuple(out):
    result = []
    for index in range(len(out[0])):
        val = [x[index] for x in out]
        result.append(tuple(val))
    return result

info_LOI = ['project', 'class', 'method', 'merge commit']

list_values = soot_results.columns.tolist()
remove_columns = ['project', 'class', 'method', 'merge commit', 'Time']
analysis = [coluna for coluna in list_values if coluna not in remove_columns]

left_right_analysis = list(set([x.replace("left right ", "") for x in analysis if "left right " in x]))
analysis_name = list(set([x.replace("left right ", "").replace("right left ", "") for x in analysis]))

# Lista dos elementos
elements = analysis_name
combinations_list = []
# Gerar todas as combinações possíveis de 2 a 4 elementos sem repetições
for length in range(1, len(elements) + 1):
    for combination in combinations(elements, length):
        combinations_list.append(list(combination))

print(combinations_list)

# gerando todas as combinações possíveis

analysis_combination = []
for i in combinations_list:
    analysis_combination.append(get_name_analysis(i))
print(analysis_combination)

# Lista dos elementos
elements = analysis_name
combinations_list = []
# Gerar todas as combinações possíveis de 2 a 4 elementos sem repetições
for length in range(1, len(elements) + 1):
    for combination in combinations(elements, length):
        combinations_list.append(list(combination))

print(combinations_list)

# gerando todas as combinações possíveis

analysis_combination = []
for i in combinations_list:
    analysis_combination.append(get_name_analysis(i))
print(analysis_combination)


#Escolhendo qual o melhor resultado com base no Algoritmo de comparação
best = Longest()

best_combination_name = "../miningframework/output/results/best_combination_log.txt"

# Abra o arquivo para escrita
with open(best_combination_name, "w") as best_combination_file:
    # Escreva os valores das variáveis no arquivo

    for first in analysis_combination:
        print(first)
        r_first = calculate_matrix(first)

        print("Combination:", count_fp_fn(r_first))
        actual_combination = best.confusion_matrix(count_fp_fn(r_first), first)

        best_combination_file.write(f"\n\nCombination: {first}")
        best_combination_file.write(f"Precision: {actual_combination['precision']:.2f}" if isinstance(actual_combination['precision'], (int, float)) else f"Accuracy: {actual_combination['precision']}")
        best_combination_file.write(f"Recall: {actual_combination['recall']:.2f}" if isinstance(actual_combination['recall'], (int, float)) else f"Accuracy: {actual_combination['recall']}")
        best_combination_file.write(f"F1 Score: {actual_combination['f1_score']:.2f}" if isinstance(actual_combination['f1_score'], (int, float)) else f"Accuracy: {actual_combination['f1_score']}")
        best_combination_file.write(f"Accuracy: {actual_combination['accuracy']:.2f}" if isinstance(actual_combination['accuracy'], (int, float)) else f"Accuracy: {actual_combination['accuracy']}")

    best_combination_file.write("\n\nThe best:")
    best_combination_file.write(f"\nPrecision: {best.maiorPrecision:.2f}, {remove_nested_best(best.mPrecision)}\n")
    best_combination_file.write(f"\nRecall: {best.maiorRecall:.2f}, {remove_nested_best(best.mRecall)}\n")
    best_combination_file.write(f"\nF1-score: {best.maiorF1:.2f}, {remove_nested_best(best.mF1)}\n")
    best_combination_file.write(f"\nAccuracy: {best.maiorAcuracia:.2f}, {remove_nested_best(best.mAcuracia)}\n")

    print(f"Precision: {best.maiorPrecision:.2f}", remove_nested_best(best.mPrecision))
    print(f"Recall: {best.maiorRecall:.2f}", remove_nested_best(best.mRecall))
    print(f"F1-score: {best.maiorF1:.2f}", remove_nested_best(best.mF1))
    print(f"Accuracy: {best.maiorAcuracia:.2f}", remove_nested_best(best.mAcuracia))


data = {
    'Metric': ['Precision', 'Recall', 'F1-score', 'Accuracy'],
    'Value': [round(best.maiorPrecision, 2), round(best.maiorRecall, 2), round(best.maiorF1, 2), round(best.maiorAcuracia, 2)],
   'Analyses': [str(to_string_as_set(get_reverse_name(remove_nested_best(best.mPrecision))))[:255],
                str(to_string_as_set(get_reverse_name(remove_nested_best(best.mRecall))))[:255],
                str(to_string_as_set(get_reverse_name(remove_nested_best(best.mF1))))[:255],
                str(to_string_as_set(get_reverse_name(remove_nested_best(best.mAcuracia))))[:255]]
}
dframe = pd.DataFrame(data)

fig, ax = plt.subplots(figsize=(12, 4))
ax.axis('tight')
ax.axis('off')

table = ax.table(cellText=dframe.values, colLabels=dframe.columns, cellLoc='center', loc='center')
table.auto_set_font_size(False)
table.set_fontsize(12)
table.scale(1.2, 1.2)

for i, col in enumerate(dframe.columns):
    col_width = max([len(str(val)) for val in dframe[col]])
    table.auto_set_column_width(i)
    table.auto_set_column_width(col_width)

plt.title("Result of the best combinations", y=0.8)

plt.savefig('../miningframework/output/results/best_combinations.jpg', dpi=300, bbox_inches='tight', pad_inches=0.5)

data = {
    'Metric': ['Precision', 'Recall', 'F1-score', 'Accuracy'],
    'Value': [round(best.maiorPrecision, 2), round(best.maiorRecall, 2), round(best.maiorF1, 2), round(best.maiorAcuracia, 2)],
   'Analyses': [str(get_reverse_name(best.mPrecision)), 
                str(get_reverse_name(best.mRecall)), 
                str(get_reverse_name(best.mF1)), 
                str(get_reverse_name(best.mAcuracia))]
}

dframe = pd.DataFrame(data)

nome_arquivo = "../miningframework/output/results/best_combinations.csv"

dframe.to_csv(nome_arquivo, sep=';', index=False)

df_t = pd.read_csv('../miningframework/output/results/times/resultTime-1.csv', sep=';', encoding='utf-8', on_bad_lines='skip', low_memory=False)

with_config = True

colums = df_t.columns

best.mPrecision = remove_nested_best(best.mPrecision)
best.mRecall = remove_nested_best(best.mRecall)
best.mF1 = remove_nested_best(best.mF1)
best.mAcuracia = remove_nested_best(best.mAcuracia)

print("Analyzing", best.mPrecision)
mean_p = get_mean_metric(best.mPrecision, with_config)
median_p = get_median_metric(best.mPrecision, with_config)
# sum_p = get_sum_metric(best.mPrecision, with_config)
std_p = get_std_metric(best.mPrecision, with_config)

# out_precision = f"Mean: {mean_p} Median: {median_p} Sum: {sum_p} Standard: {std_p}"
out_precision = [mean_p, median_p, std_p]

print(out_precision)

print("Analyzing", remove_nested_best(best.mRecall))
mean_p = get_mean_metric(best.mRecall, with_config)
median_p = get_median_metric(best.mRecall, with_config)
# sum_p = get_sum_metric(best.mRecall, with_config)
std_p = get_std_metric(best.mRecall, with_config)

# out_recall = f"Mean: {mean_p} Median: {median_p} Sum: {sum_p} Standard: {std_p}"
out_recall = [mean_p, median_p, std_p]

print(out_recall)

print("Analyzing", best.mF1)

mean_p = get_mean_metric(best.mF1, with_config)
median_p = get_median_metric(best.mF1, with_config)
# sum_p = get_sum_metric(best.mF1, with_config)
std_p = get_std_metric(best.mF1, with_config)

# out_f1 = f"Mean: {mean_p} Median: {median_p} Sum: {sum_p} Standard: {std_p}"
out_f1 = [mean_p, median_p, std_p]
print(out_f1)

print("Analyzing", best.mAcuracia)
mean_p = get_mean_metric(best.mAcuracia, with_config)
median_p = get_median_metric(best.mAcuracia, with_config)
# sum_p = get_sum_metric(best.mAcuracia, with_config)
std_p = get_std_metric(best.mAcuracia, with_config)

# out_accuracy = f"Mean: {mean_p}\nMedian: {median_p}\nSum: {sum_p}\nStandard: {std_p}\n"
out_accuracy = [mean_p, median_p, std_p]

data = {
    'Metric': ['Precision', 'Recall', 'F1-score', 'Accuracy'],
    'Value': [round(best.maiorPrecision, 2), round(best.maiorRecall, 2), round(best.maiorF1, 2), round(best.maiorAcuracia, 2)],
    'Analyses': [to_string_as_set(get_reverse_name(best.mPrecision)), to_string_as_set(get_reverse_name(best.mRecall)), to_string_as_set(get_reverse_name(best.mF1)), to_string_as_set(get_reverse_name(best.mAcuracia))],
    'Time (s) (mean, median, standard)': [convert_list_to_tuple(out_precision), convert_list_to_tuple(out_recall), convert_list_to_tuple(out_f1), convert_list_to_tuple(out_accuracy)]
}

dframe = pd.DataFrame(data)

fig, ax = plt.subplots(figsize=(12, 4))
ax.axis('tight')
ax.axis('off')

table = ax.table(cellText=dframe.values, colLabels=dframe.columns, cellLoc='center', loc='center')
table.auto_set_font_size(False)
table.set_fontsize(12)
table.scale(1.2, 1.2)

for i, col in enumerate(dframe.columns):
    col_width = max([len(str(val)) for val in dframe[col]])
    table.auto_set_column_width(i)
    table.auto_set_column_width(col_width)

plt.title("Result of the best combinations and time", y=0.8)

plt.savefig('../miningframework/output/results/best_combinations_time.jpg', dpi=300, bbox_inches='tight', pad_inches=0.5)


nome_arquivo = "../miningframework/output/results/best_combinations_time.csv"

dframe.to_csv(nome_arquivo, sep=';', index=False)

best_lists = [best.mPrecision, best.mRecall, best.mF1, best.mAcuracia]

merged_dict = {'Metrics': ['precision', 'recall', 'f1_score', 'accuracy']}

for actual_best in best_lists:
    size_list = min(len(l) for l in actual_best)
    smaller_lists = [l for l in actual_best if len(l) == size_list]
    for smaller in smaller_lists:

        if smaller not in list(merged_dict.keys()):
            m_smaller = calculate_matrix(smaller)

            actual_dict = best.confusion_matrix(count_fp_fn(m_smaller), smaller)
            l_aux = []
            for m in merged_dict['Metrics']:
                l_aux.append(format(actual_dict[m], '.2f'))

            original_name = sum(get_reverse_name([smaller]), [])
            key = ' or '.join(original_name)
            merged_dict[key] = l_aux
import pandas as pd
import matplotlib.pyplot as plt

# Dicionário de dados
data = merged_dict

# Criar um DataFrame a partir do dicionário
df = pd.DataFrame(data)

# Criar uma figura vazia
fig, ax = plt.subplots(figsize=(10, 5))

# Desativar os eixos
ax.axis('off')

# Criar uma tabela a partir do DataFrame
table = ax.table(cellText=df.values, colLabels=df.columns, cellLoc='center', loc='center')

# Adicionar um título à tabela

plt.title("Analyses with best metrics", y=0.7)

# Ajustar o layout da tabela
table.auto_set_font_size(False)
table.set_fontsize(12)
table.scale(1.2, 1.2)

# Ajustar o tamanho das colunas com base no texto
table.auto_set_column_width([0, 1, 2, 3])

# Salvar a tabela como um arquivo JPG
plt.savefig('../miningframework/output/results/table_with_best_analyses.jpg', format='jpg', bbox_inches='tight', dpi=300)

