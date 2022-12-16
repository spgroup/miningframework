import pandas as pd
import os
import seaborn as sns
import datetime
import chardet
import time
from pandas.api.types import is_number
import matplotlib.pyplot as plt
import numpy as np
import itertools

from matplotlib import pyplot as plt
from fpdf import FPDF

class ReportAnalysis:

    path_ground_truth = ''
    ground_truth_name = ''

    def __init__(self, path_ground_truth, ground_truth_name):
        self.path_ground_truth = path_ground_truth
        self.ground_truth_name = ground_truth_name
        self.generate_results()

    @staticmethod
    def generate_results():
        FP = 0
        TP = 0
        FN = 0
        TN = 0

        print("Reading analyses execution results...")
        soot_results = pd.read_csv('../miningframework/output/data/soot-results.csv', sep=';', encoding='latin-1', on_bad_lines='skip', low_memory=False)

        loi = pd.read_csv(path_ground_truth, sep=';', encoding='latin-1', on_bad_lines='skip', low_memory=False)

        print("Generating results...")

        LOIGroundTruth = []
        for LOI in loi[ground_truth_name]:
            LOIGroundTruth.append(LOI)

        index = 0
        result = {"True Negative":0, "False Negative":0, "True Positive":0, "False Positive":0}

        for (confluence, OA, lrPDG, rlPDG, lrDP, rlDF) in zip(soot_results["Confluence Inter"], soot_results["OA Inter"], soot_results["left right PDG"], soot_results["right left PDG"], soot_results["left right DFP-Inter"], soot_results["right left DFP-Inter"]):
            analysesORResult = False
            error = False
            if ("true" in str(confluence).lower() or "true" in str(OA).lower() or "true" in str(lrPDG).lower() or "true" in str(rlPDG).lower() or "true" in str(lrDP).lower() or "true" in str(rlDF).lower()):
                analysesORResult = True
            elif ("false" in str(confluence).lower() or "false" in str(OA).lower() or "false" in str(lrPDG).lower() or "false" in str(rlPDG).lower() or "false" in str(lrDP).lower() or "false" in str(rlDF).lower()):
                analysesORResult = False
            else:
                error = True

            if (LOIGroundTruth[index] == "No"):
                if (analysesORResult):
                    ReportAnalysis.updating_results(result, LOIGroundTruth[index], "False Positive", error)
                else:
                    ReportAnalysis.updating_results(result, LOIGroundTruth[index], "True Negative", error)

            if (LOIGroundTruth[index] == "Yes"):
                if (analysesORResult):
                    ReportAnalysis.updating_results(result, LOIGroundTruth[index], "True Positive", error)
                else:
                    ReportAnalysis.updating_results(result, LOIGroundTruth[index], "False Negative", error)

            index = index+1

        print(result)

        FP = result["False Positive"]
        TP = result["True Positive"]
        FN = result["False Negative"]
        TN = result["True Negative"]

        sensitivity = (TP / (TP + FN))
        precision = (TP / (TP + FP))
        f1_score = (2*TP / (2*TP + FP + FN))
        accuracy = ((TP + TN) / (FP + TP + TN + FN))

        # variable pdf
        pdf = FPDF()

        # Add a page
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

        cm = np.array([[TP,  FP], [FN, TN]])
        normalize = False
        target_names = ['Positive', 'Negative']
        title = "Confusion Matrix"

        cmap = plt.get_cmap('Blues')

        plt.figure(figsize=(8, 6))
        plt.imshow(cm, interpolation='nearest', cmap=cmap)
        plt.title(title, fontsize=16)
        plt.colorbar()

        if target_names is not None:
            tick_marks = np.arange(len(target_names))
            plt.xticks(tick_marks, target_names, rotation=45, fontsize=16)
            plt.yticks(tick_marks, target_names, fontsize=16)

        if normalize:
            cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]

        thresh = cm.max() / 1.5 if normalize else cm.max() / 2
        for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
            if normalize:
                plt.text(j, i, "{:0.4f}".format(cm[i, j]),
                         horizontalalignment="center",
                         color="white" if cm[i, j] > thresh else "black", fontsize=23)
            else:
                plt.text(j, i, "{:,}".format(cm[i, j]),
                         horizontalalignment="center",
                         color="white" if cm[i, j] > thresh else "black", fontsize=23)
        plt.tight_layout()

        plt.savefig("confusion_matrix.jpg")

        pdf.image("confusion_matrix.jpg", x = None, y = None, w = 160, h = 110, type = 'jpg', link = 'confusion_matrix.jpg')

        # save the pdf with name .pdf
        pdf.output("results.pdf")
        print("Results in results.pdf")

    @staticmethod
    def updating_results(result, LOI, index, error):
        if (LOI != "-" and not error):
            if (result.get(index) != None):
                result[index] = result.get(index) + 1
            else:
                result[index] = 1

path_ground_truth = "../miningframework/input/LOI.csv"
ground_truth_name = "Locally Observable Interference"

ReportAnalysis(path_ground_truth, ground_truth_name)
