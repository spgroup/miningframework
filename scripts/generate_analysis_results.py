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
        for (project, merge_commit, class_name, method, LOI) in zip(loi["Project"], loi["Merge Commit"], loi["Class Name"], loi["Method or field declaration changed by the two merged branches"], loi[ground_truth_name]):
            LOIGroundTruth.append((project, merge_commit, class_name, method, LOI))

        position = 0
        for (project, class_name, method, merge_commit, confluence, OA, lrPDG, rlPDG, lrDP, rlDF) in zip(soot_results["project"],  soot_results["class"],  soot_results["method"],  soot_results["merge commit"], soot_results["Confluence Inter"], soot_results["OA Inter"], soot_results["left right PDG"], soot_results["right left PDG"], soot_results["left right DFP-Inter"], soot_results["right left DFP-Inter"]):

            analysesORResult = False
            error = False
            list_results = [str(confluence).lower(), str(OA).lower(), str(lrPDG).lower(), str(rlPDG).lower(), str(lrDP).lower(), str(rlDF).lower()]
            if ("true" == str(confluence).lower() or "true" == str(OA).lower() or "true" == str(lrPDG).lower() or "true" == str(rlPDG).lower() or "true" == str(lrDP).lower() or "true" == str(rlDF).lower()):
                analysesORResult = True
            elif ("false" in list_results):
                analysesORResult = False
            else:
                error = True

            project_actual, merge_commit_actual, class_name_actual, method_actual, loi_actual = ("", "", "", "", "")
            for (project_current, merge_commit_current, class_name_current, method_current, LOI_current) in LOIGroundTruth:

                if (project_current, merge_commit_current, class_name_current, method_current) == (project, merge_commit, class_name, method):
                    project_actual, merge_commit_actual, class_name_actual, method_actual, loi_actual = project_current, merge_commit_current, class_name_current, method_current, LOI_current
            if (loi_actual != "-" and not error and project_actual == project and merge_commit_actual == merge_commit):
                if (loi_actual == "No"):
                    if (analysesORResult):
                        FP = FP + 1
                    else:
                        TN = TN + 1

                if (loi_actual == "Yes"):
                    if (analysesORResult):
                        TP = TP + 1
                    else:
                        FN = FN + 1
            position = position + 1
        sensitivity = 0 if ((TP + FN) == 0) else (TP / (TP + FN))
        precision = 0 if ((TP + FP) == 0) else (TP / (TP + FP))
        f1_score = 0 if ((2*TP + FP + FN) == 0) else (2*TP / (2*TP + FP + FN))
        accuracy = 0 if ((FP + TP + TN + FN) == 0) else ((TP + TN) / (FP + TP + TN + FN))

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

        # save the pdf with name .pdf
        pdf.output("output/data/results.pdf")
        print("Results in output/data/results.pdf")

path_ground_truth = "../miningframework/input/LOI.csv"
ground_truth_name = "Locally Observable Interference"

ReportAnalysis(path_ground_truth, ground_truth_name)
