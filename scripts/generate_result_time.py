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
import numpy as np
import statistics
import matplotlib.pyplot as plt
from fpdf import FPDF
import sys

class ResultAnalysis:

	results = []
	dataframes = []
	median = 0
	mean = 0
	stardard_deviation = 0
	n = 10

	def __init__(self, val):
		self.results = []
		self.dataframes = []
		self.median = 0
		self.mean = 0
		self.stardard_deviation = 0
		self.n = val
		self.generate_results()
		

	def calcular(self):
		# Median
		self.median = statistics.median(self.results)

		# Mean
		self.mean = statistics.mean(self.results)

		# Standard Deviation
		self.stardard_deviation = statistics.stdev(self.results)

	#Loading files
	def load_files(self):
		arquivos_csv = [str('resultTime-'+str(i+1)+'.csv') for i in range(self.n)]

		for arquivo in arquivos_csv:
			print(arquivo)
			df = pd.read_csv(arquivo, sep=';', encoding='latin-1', on_bad_lines='skip', low_memory=False)
			self.dataframes.append(df)
	
	#Sum the lines from files	
	def sum_lines(self):
		mean_line = []
		num_lines = self.dataframes[0].shape[0]
		for j in range(num_lines):
			sum_actual = 0
			for i in range(self.n):
				sum_actual = sum_actual + float(self.dataframes[i].iloc[j].sum())
			mean_line.append(sum_actual/14)

		self.results = mean_line
	
	#Sum the columns from files	
	def sum_columns(self):
		total = {0:0, 1:0, 2:0, 3:0, 4:0, 5:0, 6:0, 7:0, 8:0, 9:0, 10:0, 11:0, 12:0, 13:0}
		num_lines = self.dataframes[0].shape[0]

		for i in range(self.n):
			dframe = self.dataframes[i]
			cont = 0
			for c in dframe.columns:
				total[cont] = total[cont] + dframe[c].sum()
				cont = cont + 1
		self.results = []
		# Calculating mean
		for i in total.values():
			self.results.append(i/num_lines)

	# Save the result in a pdf file
	def save_pdf(self, file_name, file_name_img):
		self.calcular()
		pdf = FPDF()

		# Add a page
		pdf.add_page()

		# set style and size of font
		# that you want in the pdf
		pdf.set_font("Arial", size = 15)

		# create a cell
		pdf.cell(200, 10, txt = "Results",
				ln = 1, align = 'C')

		pdf.cell(200, 10, txt = ("Means: "+str(round(self.mean, 4))),
				ln = 2, align = 'L')

		pdf.cell(200, 10, txt = ("Median: "+str(round(self.median, 4))),
				ln = 2, align = 'L')

		pdf.cell(200, 10, txt = ("Standard Deviation: "+str(round(self.stardard_deviation, 4))),
				ln = 2, align = 'L')
		
		pdf.image(file_name_img, x = 10, y = None, w = 200, h = 90, type = 'jpg', link = file_name_img)

		# save the pdf with name file_name
		pdf.output(file_name)
		print("Saving results in", "output/results/"+file_name)

	def plot_by_scenarios(self):
		fig, ax = plt.subplots(figsize=(8, 3))

		data_x = [self.results]

		# Create a list of colors for the boxplots based on the number of features you have
		boxplots_colors = ['yellowgreen']

		# Boxplot data
		bp = ax.boxplot(data_x, patch_artist = True, vert = False)

		# Change to the desired color and add transparency
		for patch, color in zip(bp['boxes'], boxplots_colors):
			patch.set_facecolor(color)
			patch.set_alpha(0.4)

		# Create a list of colors for the violin plots based on the number of features you have
		violin_colors = ['thistle']

		# Violinplot data
		vp = ax.violinplot(data_x, points=500, 
					showmeans=False, showextrema=False, showmedians=False, vert=False)

		for idx, b in enumerate(vp['bodies']):
			# Get the center of the plot
			m = np.mean(b.get_paths()[0].vertices[:, 0])
			# Modify it so we only see the upper half of the violin plot
			b.get_paths()[0].vertices[:, 1] = np.clip(b.get_paths()[0].vertices[:, 1], idx+1, idx+2)
			# Change to the desired color
			b.set_color(violin_colors[idx])

		# Create a list of colors for the scatter plots based on the number of features you have
		scatter_colors = ['tomato', 'darksalmon']

		# Scatterplot data
		for idx, features in enumerate(data_x):
			# Add jitter effect so the features do not overlap on the y-axis
			y = np.full(len(features), idx + .8)
			idxs = np.arange(len(y))
			out = y.astype(float)
			out.flat[idxs] += np.random.uniform(low=-.05, high=.05, size=len(idxs))
			y = out
			plt.scatter(features, y, s=.3, c=scatter_colors[idx])

		plt.subplots_adjust(left=0.25)
		plt.yticks(np.arange(1,3), ['Means of the scenarios'])  # Set text labels.
		plt.xlabel('Values')
		plt.title("Results by scenarios")
		plt.savefig("results_by_scenarios.jpg", dpi=300)

		self.save_pdf("results_scenarios.pdf", "results_by_scenarios.jpg")


	def plot_by_execution(self):
		fig, ax = plt.subplots(figsize=(8, 3))

		data_x = [self.results]

		# Create a list of colors for the boxplots based on the number of features you have
		boxplots_colors = ['yellowgreen']

		# Boxplot data
		bp = ax.boxplot(data_x, patch_artist = True, vert = False)

		# Change to the desired color and add transparency
		for patch, color in zip(bp['boxes'], boxplots_colors):
			patch.set_facecolor(color)
			patch.set_alpha(0.4)

		# Create a list of colors for the violin plots based on the number of features you have
		violin_colors = ['thistle']

		# Violinplot data
		vp = ax.violinplot(data_x, points=500, 
					showmeans=False, showextrema=False, showmedians=False, vert=False)

		for idx, b in enumerate(vp['bodies']):
			# Get the center of the plot
			m = np.mean(b.get_paths()[0].vertices[:, 0])
			# Modify it so we only see the upper half of the violin plot
			b.get_paths()[0].vertices[:, 1] = np.clip(b.get_paths()[0].vertices[:, 1], idx+1, idx+2)
			# Change to the desired color
			b.set_color(violin_colors[idx])

		# Create a list of colors for the scatter plots based on the number of features you have
		scatter_colors = ['tomato', 'darksalmon']

		# Scatterplot data
		for idx, features in enumerate(data_x):
			# Add jitter effect so the features do not overlap on the y-axis
			y = np.full(len(features), idx + .8)
			idxs = np.arange(len(y))
			out = y.astype(float)
			out.flat[idxs] += np.random.uniform(low=-.05, high=.05, size=len(idxs))
			y = out
			plt.scatter(features, y, s=.3, c=scatter_colors[idx])

		plt.subplots_adjust(left=0.25)
		plt.yticks(np.arange(1,3), ['Means of the executions'])  # Set text labels.
		plt.xlabel('Values')
		plt.title("Results by execution")
		plt.savefig("results_by_execution.jpg", dpi=300)

		self.save_pdf("results_execution.pdf", "results_by_execution.jpg")

	def generate_results(self):
		self.load_files()
		self.sum_lines()
		self.plot_by_scenarios()
		self.sum_columns()
		self.plot_by_execution()


n = 10

if len(sys.argv) > 1:
	n = int(sys.argv[1])
else:
	n = 10

ResultAnalysis(n)
