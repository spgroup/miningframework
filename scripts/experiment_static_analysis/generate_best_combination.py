import pandas as pd
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
    element_count = Counter(list_result)
    result = []
    for element, count in element_count.items():
        if count > 1:
            result.append((str(element)+": "+str(count)))
    return result

class Longest:
    higher_precision_value = -1.0
    higher_recall_value = -1.0
    higher_F1_value = -1.0
    higher_accuracy_value = -1.0
    higher_precision_analyses_names = []
    higher_recall_analyses_names = []
    higher_F1_analyses_names = []
    higher_accuracy_analyses_names = []
    mPrecision_t = []
    mRecall_t = []
    mF1_t = []
    mAcuracia_t = []

    def __init__(self):
        self.higher_precision_value = -1.0
        self.higher_recall_value = -1.0
        self.higher_F1_value = -1.0
        self.higher_accuracy_value = -1.0
        self.higher_precision_analyses_names = []
        self.higher_recall_analyses_names = []
        self.higher_F1_analyses_names = []
        self.higher_accuracy_analyses_names = []
        self.mPrecision_t = []
        self.mRecall_t = []
        self.mF1_t = []
        self.mAcuracia_t = []


    def confusion_matrix(self, options, values_elem):

        tp, fp, tn, fn = 0, 0, 0, 0

        for option in options:
            if "TRUE POSITIVE" in option:
                tp = int(option.split(': ')[1])
            elif "FALSE POSITIVE" in option:
                fp = int(option.split(': ')[1])
            elif "TRUE NEGATIVE" in option:
                tn = int(option.split(': ')[1])
            elif "FALSE NEGATIVE" in option:
                fn = int(option.split(': ')[1])
       
        # Calculating metrics and values
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

            # selecting the best result based on the metrics

            if precision != '-' and precision > self.higher_precision_value:
                self.higher_precision_value = precision
                self.higher_precision_analyses_names = []
                self.higher_precision_analyses_names.append(values_elem)

            if precision != '-' and precision == self.higher_precision_value:
                if values_elem not in self.higher_precision_analyses_names:
                    self.higher_precision_analyses_names.append(values_elem)

            if recall != '-' and recall > self.higher_recall_value:
                self.higher_recall_value = recall
                self.higher_recall_analyses_names = []
                self.higher_recall_analyses_names.append(values_elem)

            if recall != '-' and recall == self.higher_recall_value:
                if values_elem not in self.higher_recall_analyses_names:
                    self.higher_recall_analyses_names.append(values_elem)

            if f1_score != '-' and f1_score > self.higher_F1_value:
                self.higher_F1_value = f1_score
                self.higher_F1_analyses_names = []
                self.higher_F1_analyses_names.append(values_elem)

            if f1_score != '-' and f1_score == self.higher_F1_value:
                if values_elem not in self.higher_F1_analyses_names:
                    self.higher_F1_analyses_names.append(values_elem)

            if accuracy != '-' and accuracy > self.higher_accuracy_value:
                self.higher_accuracy_value = accuracy
                self.higher_accuracy_analyses_names = []
                self.higher_accuracy_analyses_names.append(values_elem)

            if accuracy != '-' and accuracy == self.higher_accuracy_value:
                if values_elem not in self.higher_accuracy_analyses_names:
                    self.higher_accuracy_analyses_names.append(values_elem)

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

def sum_n_esimo_element(list_of_list, n):
    return sum(float(str(sub_list[n]).replace(",", ".")) for sub_list in list_of_list)

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
        if "Confluence" in i:
            actual_name = i.replace("Confluence ", "Confluence 1 ")
            if config:
                names.append("Configure Soot "+actual_name)
            names.append("Time to perform "+actual_name)

            actual_name = i.replace("Confluence ", "Confluence 2 ")

            if config:
                names.append("Configure Soot "+actual_name)

            names.append("Time to perform "+actual_name)

        elif "left right" in i:
            actual_name = i.replace("left right ", "")
            if "DF" in i:
                actual_name = actual_name.replace("-", " ")
            else:
                actual_name = actual_name.replace("-", "")

            if config:
                names.append("Configure Soot "+actual_name+" left-right")
                names.append("Configure Soot "+actual_name+" right-left")
            names.append("Time to perform "+actual_name+" left-right")
            names.append("Time to perform "+actual_name+" right-left")
        elif "right left" not in str(i):
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

def concat_tuple(list_tuple):
    return ' '.join([str(tuple) for tuple in list_tuple])

def convert_list_to_tuple(out):
    result = []
    for index in range(len(out[0])):
        val = [x[index] for x in out]
        result.append(tuple(val))
    return concat_tuple(result)

info_LOI = ['project', 'class', 'method', 'merge commit']

list_values = soot_results.columns.tolist()
remove_columns = ['project', 'class', 'method', 'merge commit', 'Time']
analysis = [coluna for coluna in list_values if coluna not in remove_columns]

left_right_analysis = list(set([x.replace("left right ", "") for x in analysis if "left right " in x]))
analysis_name = list(set([x.replace("left right ", "").replace("right left ", "") for x in analysis]))

# list with the names of the executed analyses
elements = analysis_name
combinations_list = []

# generate all possible combinations of 2 to 4 elements without repetitions
for length in range(1, len(elements) + 1):
    for combination in combinations(elements, length):
        combinations_list.append(list(combination))

# returning the names of the combinations
analysis_combination = []
for i in combinations_list:
    analysis_combination.append(get_name_analysis(i))

# selecting the best result based on the Comparison Algorithm
best = Longest()

best_combination_name = "../miningframework/output/results/best_combination_log.txt"

with open(best_combination_name, "w") as best_combination_file:

    for combination_value in analysis_combination:

        matrix_combination = calculate_matrix(combination_value)

        print("Combination:", combination_value)
        print(count_fp_fn(matrix_combination))
        actual_combination = best.confusion_matrix(count_fp_fn(matrix_combination), combination_value)

        best_combination_file.write(f"\n\nCombination: {combination_value}")
        best_combination_file.write(f"\n\nConfusion Matrix: {count_fp_fn(matrix_combination)}")
        best_combination_file.write(f"\nPrecision: {actual_combination['precision']:.2f}" if isinstance(actual_combination['precision'], (int, float)) else f"Accuracy: {actual_combination['precision']}")
        best_combination_file.write(f"\nRecall: {actual_combination['recall']:.2f}" if isinstance(actual_combination['recall'], (int, float)) else f"Accuracy: {actual_combination['recall']}")
        best_combination_file.write(f"\nF1 Score: {actual_combination['f1_score']:.2f}" if isinstance(actual_combination['f1_score'], (int, float)) else f"Accuracy: {actual_combination['f1_score']}")
        best_combination_file.write(f"\nAccuracy: {actual_combination['accuracy']:.2f}" if isinstance(actual_combination['accuracy'], (int, float)) else f"Accuracy: {actual_combination['accuracy']}")

    best_combination_file.write("\n\nThe best:")
    best_combination_file.write(f"\nPrecision: {best.higher_precision_value:.2f}, {remove_nested_best(best.higher_precision_analyses_names)}\n")
    best_combination_file.write(f"\nRecall: {best.higher_recall_value:.2f}, {remove_nested_best(best.higher_recall_analyses_names)}\n")
    best_combination_file.write(f"\nF1-score: {best.higher_F1_value:.2f}, {remove_nested_best(best.higher_F1_analyses_names)}\n")
    best_combination_file.write(f"\nAccuracy: {best.higher_accuracy_value:.2f}, {remove_nested_best(best.higher_accuracy_analyses_names)}\n")

    print(f"Precision: {best.higher_precision_value:.2f}", remove_nested_best(best.higher_precision_analyses_names))
    print(f"Recall: {best.higher_recall_value:.2f}", remove_nested_best(best.higher_recall_analyses_names))
    print(f"F1-score: {best.higher_F1_value:.2f}", remove_nested_best(best.higher_F1_analyses_names))
    print(f"Accuracy: {best.higher_accuracy_value:.2f}", remove_nested_best(best.higher_accuracy_analyses_names))

data = {
    'Metric': ['Precision', 'Recall', 'F1-score', 'Accuracy'],
    'Value': [round(best.higher_precision_value, 2), round(best.higher_recall_value, 2), round(best.higher_F1_value, 2), round(best.higher_accuracy_value, 2)],
   'Analyses': [str(to_string_as_set(get_reverse_name(remove_nested_best(best.higher_precision_analyses_names))))[:255],
                str(to_string_as_set(get_reverse_name(remove_nested_best(best.higher_recall_analyses_names))))[:255],
                str(to_string_as_set(get_reverse_name(remove_nested_best(best.higher_F1_analyses_names))))[:255],
                str(to_string_as_set(get_reverse_name(remove_nested_best(best.higher_accuracy_analyses_names))))[:255]]
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
    'Value': [round(best.higher_precision_value, 2), round(best.higher_recall_value, 2), round(best.higher_F1_value, 2), round(best.higher_accuracy_value, 2)],
    'Analyses': [str(get_reverse_name(best.higher_precision_analyses_names)),
                 str(get_reverse_name(best.higher_recall_analyses_names)),
                 str(get_reverse_name(best.higher_F1_analyses_names)),
                 str(get_reverse_name(best.higher_accuracy_analyses_names))]
}

dframe = pd.DataFrame(data)

nome_arquivo = "../miningframework/output/results/best_combinations.csv"

dframe.to_csv(nome_arquivo, sep=';', index=False)

df_t = pd.read_csv('../miningframework/output/results/times/resultTime-1.csv', sep=';', encoding='utf-8', on_bad_lines='skip', low_memory=False)

with_config = True

colums = df_t.columns

best.higher_precision_analyses_names = remove_nested_best(best.higher_precision_analyses_names)
best.higher_recall_analyses_names = remove_nested_best(best.higher_recall_analyses_names)
best.higher_F1_analyses_names = remove_nested_best(best.higher_F1_analyses_names)
best.higher_accuracy_analyses_names = remove_nested_best(best.higher_accuracy_analyses_names)

print("Analyzing", best.higher_precision_analyses_names)

mean_p = get_mean_metric(best.higher_precision_analyses_names, with_config)
median_p = get_median_metric(best.higher_precision_analyses_names, with_config)
std_p = get_std_metric(best.higher_precision_analyses_names, with_config)

# out_precision = f"Mean: {mean_p} Median: {median_p} Sum: {sum_p} Standard: {std_p}"
out_precision = [mean_p, median_p, std_p]

print("Analyzing", best.higher_recall_analyses_names)

mean_p = get_mean_metric(best.higher_recall_analyses_names, with_config)
median_p = get_median_metric(best.higher_recall_analyses_names, with_config)
std_p = get_std_metric(best.higher_recall_analyses_names, with_config)

# out_recall = f"Mean: {mean_p} Median: {median_p} Sum: {sum_p} Standard: {std_p}"
out_recall = [mean_p, median_p, std_p]

print("Analyzing", best.higher_F1_analyses_names)

mean_p = get_mean_metric(best.higher_F1_analyses_names, with_config)
median_p = get_median_metric(best.higher_F1_analyses_names, with_config)
std_p = get_std_metric(best.higher_F1_analyses_names, with_config)

# out_f1 = f"Mean: {mean_p} Median: {median_p} Sum: {sum_p} Standard: {std_p}"
out_f1 = [mean_p, median_p, std_p]

print("Analyzing", best.higher_accuracy_analyses_names)

mean_p = get_mean_metric(best.higher_accuracy_analyses_names, with_config)
median_p = get_median_metric(best.higher_accuracy_analyses_names, with_config)
std_p = get_std_metric(best.higher_accuracy_analyses_names, with_config)

# out_accuracy = f"Mean: {mean_p}\nMedian: {median_p}\nSum: {sum_p}\nStandard: {std_p}\n"
out_accuracy = [mean_p, median_p, std_p]

data = {
    'Metric': ['Precision', 'Recall', 'F1-score', 'Accuracy'],
    'Value': [round(best.higher_precision_value, 2), round(best.higher_recall_value, 2), round(best.higher_F1_value, 2), round(best.higher_accuracy_value, 2)],
    'Analyses': [to_string_as_set(get_reverse_name(best.higher_precision_analyses_names)), to_string_as_set(get_reverse_name(best.higher_recall_analyses_names)), to_string_as_set(get_reverse_name(best.higher_F1_analyses_names)), to_string_as_set(get_reverse_name(best.higher_accuracy_analyses_names))],
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

best_lists = [remove_nested_best(best.higher_precision_analyses_names), remove_nested_best(best.higher_recall_analyses_names), remove_nested_best(best.higher_F1_analyses_names), remove_nested_best(best.higher_accuracy_analyses_names)]

merged_dict = {'Metrics': ['precision', 'recall', 'f1_score', 'accuracy']}

for actual_best in best_lists:
    for smaller in actual_best:
        if smaller not in list(merged_dict.keys()):
            m_smaller = calculate_matrix(smaller)

            actual_dict = best.confusion_matrix(count_fp_fn(m_smaller), smaller)
            l_aux = []
            for m in merged_dict['Metrics']:
                l_aux.append(format(actual_dict[m], '.2f'))

            original_name = sum(get_reverse_name([smaller]), [])
            key = ' or '.join(original_name)
            merged_dict[key] = l_aux

data = merged_dict

df = pd.DataFrame(data)

fig, ax = plt.subplots(figsize=(10, 5))

ax.axis('off')

table = ax.table(cellText=df.values, colLabels=df.columns, cellLoc='center', loc='center')

plt.title("Analyses with best metrics", y=0.7)

table.auto_set_font_size(False)
table.set_fontsize(12)
table.scale(1.2, 1.2)

table.auto_set_column_width(range(100))

plt.savefig('../miningframework/output/results/table_with_best_analyses.jpg', format='jpg', bbox_inches='tight', dpi=300)
