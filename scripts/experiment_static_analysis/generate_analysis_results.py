import pandas as pd
import numpy as np
import itertools
from collections import Counter
from matplotlib import pyplot as plt
from fpdf import FPDF

class ReportAnalysis:

    def __init__(self, path_result, path_ground_truth):
        self.soot_results = pd.read_csv(path_result, sep=';', encoding='latin-1', on_bad_lines='skip', low_memory=False)
        self.loi = pd.read_csv(path_ground_truth, sep=';', encoding='latin-1', on_bad_lines='skip', low_memory=False)

        self.generate_results()

    def get_loi(self, project, class_name,  method, merge_commit):

        filter_scenario = (self.loi['Project'] == str(project)) & (self.loi['Merge Commit'] == str(merge_commit)) & (self.loi['Class Name'] == str(class_name)) & (self.loi['Method or field declaration changed by the two merged branches'] == str(method))
        value_LOI = ""

        if filter_scenario.any():
            value_LOI = self.loi.loc[filter_scenario, 'Locally Observable Interference'].values[0]

        return value_LOI

    def calculate_matrix_loi(self, columns):
        results = []
        info_LOI = ['project', 'class', 'method', 'merge commit']

        for index, row in self.soot_results.iterrows():
            list_values = self.soot_results.columns.tolist()
            remove_columns = ['project', 'class', 'method', 'merge commit', 'Time']
            list_values = [coluna for coluna in list_values if coluna not in remove_columns]
            values = [row[column] for column in list_values]

            values_LOI = [row[column] for column in info_LOI]

            loi_actual = self.get_loi(*values_LOI)

            or_value = any(str(value).lower() != 'false' for value in values)

            result = ""
            if or_value == True and loi_actual == 'Yes':
                result = "TRUE POSITIVE"
            elif or_value == False and loi_actual == 'No':
                result = "TRUE NEGATIVE"
            elif or_value == False and loi_actual == 'Yes':
                result = "FALSE NEGATIVE"
            elif or_value == True and loi_actual == 'No':
                result = "FALSE POSITIVE"
            results.append(result)
        return results

    def generate_results(self):

        print("Generating results...")

        FP,TP, FN, TN = 0, 0, 0, 0

        list_columns = self.soot_results.columns.tolist()

        result_matrix = self.calculate_matrix_loi(list_columns)

        for elem, count in Counter(result_matrix).items():
            if (elem == 'FALSE POSITIVE'):
                FP = count
            if (elem == 'FALSE NEGATIVE'):
                FN = count
            if (elem == 'TRUE POSITIVE'):
                TP = count
            if (elem == 'TRUE NEGATIVE'):
                TN = count

        sensitivity = 0 if ((TP + FN) == 0) else (TP / (TP + FN))
        precision = 0 if ((TP + FP) == 0) else (TP / (TP + FP))
        f1_score = 0 if ((2*TP + FP + FN) == 0) else (2*TP / (2*TP + FP + FN))
        accuracy = 0 if ((FP + TP + TN + FN) == 0) else ((TP + TN) / (FP + TP + TN + FN))

        # variable pdf
        pdf = FPDF()

        # add a page
        pdf.add_page()

        # set style and size of font
        # that you want in the pdf
        pdf.set_font("Arial", size = 15)

        # create a cell
        pdf.cell(200, 10, txt = "Results for execution",
                 ln = 1, align = 'C')

        pdf.cell(200, 10, txt = ("Precision: "+str(round(precision, 4))),
                 ln = 2, align = 'L')

        pdf.cell(200, 10, txt = ("Recall: "+str(round(sensitivity, 4))),
                 ln = 2, align = 'L')

        pdf.cell(200, 10, txt = ("F1 Score: "+str(round(f1_score, 4))),
                 ln = 2, align = 'L')

        pdf.cell(200, 10, txt = ("Accuracy: "+str(round(accuracy, 4))),
                 ln = 2, align = 'L')

        pdf.cell(200, 10, txt = ("False Positives: "+str(FP)),
                 ln = 2, align = 'L')

        pdf.cell(200, 10, txt = ("False Negatives: "+str(FN)),
                 ln = 2, align = 'L')

        pdf.cell(200, 10, txt = ("True Positives: "+str(TP)),
                 ln = 2, align = 'L')

        pdf.cell(200, 10, txt = ("True Negatives: "+str(TN)),
                 ln = 2, align = 'L')

        cm = np.array([[TP,  FP], [FN, TN]])
        normalize = False
        target_names = ['Actually Positive', ' Actually Negative']
        target_names2 = ['Predicted Positive', ' Predicted Negative']
        title = "Confusion Matrix"

        cmap = plt.get_cmap('Blues')

        plt.figure(figsize=(8, 6))
        plt.imshow(cm, interpolation='nearest', cmap=cmap)
        plt.title(title, fontsize=16)
        plt.colorbar()

        if target_names is not None:
            tick_marks = np.arange(len(target_names))
            plt.xticks(tick_marks, target_names, fontsize=16)
            plt.yticks(tick_marks, target_names2, fontsize=16)

        if normalize:
            cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]

        thresh = cm.max() / 1.5 if normalize else cm.max() / 2
        for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
            if normalize:
                plt.text(j, i, "{:0.4f}".format(cm[i, j]),
                         horizontalalignment="center",
                         color="yellow" if cm[i, j] > thresh else "black", fontsize=23)
            else:
                plt.text(j, i, "{:,}".format(cm[i, j]),
                         horizontalalignment="center",
                         color="yellow" if cm[i, j] > thresh else "black", fontsize=23)
        plt.tight_layout()

        plt.savefig("confusion_matrix.jpg")

        pdf.image("confusion_matrix.jpg", x = None, y = None, w = 160, h = 110, type = 'jpg', link = 'confusion_matrix.jpg')

        # Save the pdf with name .pdf
        pdf.output("output/data/results.pdf")
        # pdf.output("results.pdf")

        print("Results in output/data/results.pdf")

path_ground_truth = "../miningframework/input/LOI.csv"
path_result = '../miningframework/output/data/soot-results.csv'
# path_ground_truth = "LOI.csv"
# path_result = 'soot-results.csv'

print("Reading analyses execution results...")

ReportAnalysis(path_result, path_ground_truth)

