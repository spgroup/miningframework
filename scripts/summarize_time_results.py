#!/usr/bin/python

import pandas as pd
import numpy as np
import statistics
import matplotlib.pyplot as plt
from fpdf import FPDF
import sys
import os

class ResultAnalysis:

	results = []
	dataframes = []
	median = 0
	mean = 0
	stardard_deviation = 0
	n = 10
	time_analysis = []
	list_times = []
	time_by_scenario = []

	def __init__(self, val):
		self.results = []
		self.dataframes = []
		self.median = 0
		self.mean = 0
		self.stardard_deviation = 0
		self.time_analysis = []
		self.n = val
		self.list_times = []
		self.time_by_scenario = []

		self.generate_results()


	# Calculate median, mean and standard deviation for results
	def calculate(self):
		# Median
		self.median = statistics.median(self.results)

		# Mean
		self.mean = statistics.mean(self.results)

		# Standard Deviation
		self.stardard_deviation = statistics.stdev(self.results)

	# Loading files
	def load_files(self):
		files_csv = [f"resultTime-{i+1}.csv" for i in range(self.n)]

		for file in files_csv:
			if not os.path.isfile(file):
				print(f"File {file} not found.")
				continue

			try:
				df = pd.read_csv(file, sep=';', encoding='latin-1', on_bad_lines='skip', low_memory=False)
				self.dataframes.append(df)
			except Exception as e:
				print(f"Error to read file {file}: {str(e)}")

	def calculate_metrics_by_scenarios(self):
		# Median
		median = statistics.median(self.list_times)

		# Mean
		mean = statistics.mean(self.list_times)

		# Standard Deviation
		stardard_deviation = statistics.stdev(self.list_times)

		return [mean, median, stardard_deviation]

	def create_sheets(self, list_data, idx):
		df = pd.DataFrame(list_data, columns=['mean', 'median', 'stardard deviation'])
		df['mean'] = df['mean'].round(2).astype(float)
		df['median'] = df['median'].round(2).astype(float)
		df['stardard deviation'] = df['stardard deviation'].round(2).astype(float)

		print("Saving", 'output/results/sheets/results_by_scenario_execution_'+str(idx)+'.csv')
		df.to_csv('results_by_scenario_execution_'+str(idx)+'.csv', index=False)

	# Sum the lines from files and calculate the mean, median and stardard deviation
	def calculate_by_scenarios(self):
		num_lines = self.dataframes[0].shape[0]
		for i in range(self.n):
			actual_sum = 0
			list_sheets = []
			for j in range(num_lines):
				self.list_times = self.dataframes[i].iloc[j].values.tolist()
				result = self.calculate_metrics_by_scenarios()
				list_sheets.append(result)
			self.create_sheets(list_sheets, i)

	# Sum the lines from files
	def sum_lines(self):
		mean_line = []
		num_lines = self.dataframes[0].shape[0]

		for j in range(num_lines):
			actual_sum = 0
			list_aux_by_scenario = []
			for i in range(self.n):
				actual_sum = actual_sum + float(self.dataframes[i].iloc[j].astype(float).sum())
				list_aux_by_scenario.append(self.dataframes[i].iloc[j].astype(float).sum())
			self.list_times = list_aux_by_scenario
			values = self.calculate_metrics_by_scenarios()
			self.time_by_scenario.append(list_aux_by_scenario + values)
			mean_line.append(actual_sum/self.n)
		self.results = mean_line

	# Sum the executions of the files
	def sum_executions(self):
		sum_by_execution = []
		for i in range(self.n):
			dframe = self.dataframes[i]
			total = 0
			for c in dframe.columns:
				total = total + dframe[c].sum()
			sum_by_execution.append(total)
		self.results = sum_by_execution

	# Sum the columns from files
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
		self.time_analysis = []
		# Calculating mean of the configuration and performance analysis
		for i in range(0, len(total.values()), 2):
			self.results.append((total[i]+total[i+1])/num_lines/self.n)
			self.time_analysis.append(total[i+1]/num_lines/self.n)

	# Save the result in a pdf file
	def save_pdf(self, file_name, file_name_img):
		self.calculate()
		pdf = FPDF()

		# Add a page
		pdf.add_page()

		# set style and size of font
		# that you want in the pdf
		pdf.set_font("Arial", size = 15)

		# create a cell
		pdf.cell(200, 10, txt = "Results",
				ln = 1, align = 'C')

		pdf.cell(200, 10, txt = ("Mean: "+str(round(self.mean, 4))),
				ln = 2, align = 'L')

		pdf.cell(200, 10, txt = ("Median: "+str(round(self.median, 4))),
				ln = 2, align = 'L')

		pdf.cell(200, 10, txt = ("Standard Deviation: "+str(round(self.stardard_deviation, 4))),
				ln = 2, align = 'L')
		
		pdf.image(file_name_img, x = 10, y = None, w = 200, h = 90, type = 'jpg', link = file_name_img)

		# save the pdf with name file_name
		pdf.output(file_name)
		print("Saving results in", "output/results/"+file_name)

	def plot_by_variable(self, variable):
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
		plt.yticks(np.arange(1,2), ['Means of the '+variable])  # Set text labels.
		plt.subplots_adjust(bottom=0.25)
		plt.xlabel('Values (seconds)')
		plt.title("Results by "+variable)
		plt.savefig("results_by_"+variable+".jpg", dpi=300)

		self.save_pdf("results_"+variable+".pdf", "results_by_"+variable+".jpg")

	def create_sheet_by_scenario(self):
		columns_sheet_by_scenario = [str("Execution-"+str(i+1)) for i in range(self.n)]+['mean', 'median', 'stardard deviation']
		df = pd.DataFrame(self.time_by_scenario, columns=columns_sheet_by_scenario)
		df = df.astype(float)
		df = df.round(2)

		print("Saving", 'output/results/sheets/results_by_scenario_all_execution.csv')
		df.to_csv('results_by_scenario_all_execution.csv', index=False)

	def generate_results(self):
		self.load_files()
		self.sum_lines()
		self.create_sheet_by_scenario()
		self.calculate_by_scenarios()
		self.plot_by_variable("scenarios")
		self.sum_columns()
		self.plot_by_variable("analysis")
		self.results = self.time_analysis
		self.plot_by_variable("only_analysis")
		self.sum_executions()
		self.plot_by_variable("execution")

n = 10

if len(sys.argv) > 1:
	n = int(sys.argv[1])
else:
	n = 10

print("Running with n =", n)
ResultAnalysis(n)
