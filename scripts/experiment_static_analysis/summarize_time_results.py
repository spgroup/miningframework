#!/usr/bin/python

import pandas as pd
import numpy as np
import statistics
import matplotlib.pyplot as plt
from fpdf import FPDF
import os
import sys

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

	def generate_results(self):
		self.load_results_time_files()
		self.sum_lines_by_scenario()
		self.create_sheet_by_scenario()
		self.calculate_by_scenarios()
		self.plot_by_variable("scenarios")
		self.sum_columns_by_scenario()
		self.plot_by_variable("analysis_and_configuration")
		self.results = self.time_analysis
		self.plot_by_variable("analysis")
		self.sum_executions()
		self.plot_by_variable("experiment")

	# Calculate the mean, median, and standard deviation for each execution
	def calculate_stats_by_execution(self):
		# Median
		self.median = statistics.median(self.results)

		# Mean
		self.mean = statistics.mean(self.results)

		# Standard Deviation
		self.stardard_deviation = statistics.stdev(self.results)

	# Loading files
	def load_results_time_files(self):
		files_csv = [f"resultTime-{i+1}.csv" for i in range(self.n)]

		for file in files_csv:
			if not os.path.isfile(file):
				print(f"File {file} not found.")
				continue

			try:
				df = pd.read_csv(file, sep=';', encoding='utf-8', on_bad_lines='skip', low_memory=False)
				self.dataframes.append(df)
			except Exception as e:
				print(f"Error to read file {file}: {str(e)}")

	#Calculate the mean, median, and standard deviation for each scenario
	def calculate_stats_by_scenarios(self):
		# Median
		median = statistics.median(self.list_times)

		# Mean
		mean = statistics.mean(self.list_times)

		# Standard Deviation
		stardard_deviation = statistics.stdev(self.list_times)

		return [mean, median, stardard_deviation]

	# Create a spreadsheet with the mean, median, and standard deviation for each execution
	def create_sheets_scenario_by_execution(self, list_data, idx):
		df = pd.DataFrame(list_data, columns=['mean', 'median', 'stardard deviation'])
		df = df.astype(float)
		df = df.round(2)

		print("Saving", 'output/results/sheets/results_by_scenario_execution_'+str(idx)+'.csv')
		df.to_csv('results_by_scenario_execution_'+str(idx)+'.csv', index=False)

	# Sum the lines from files and calculate the mean, median and stardard deviation
	def calculate_by_scenarios(self):
		num_lines = self.dataframes[0].shape[0]
		for i in range(self.n):
			list_sheets = []
			for j in range(num_lines):
				self.list_times = self.dataframes[i].iloc[j].values.tolist()
				result = self.calculate_stats_by_scenarios()
				list_sheets.append(result)
			self.create_sheets_scenario_by_execution(list_sheets, i)

	# Sum the lines from files
	def sum_lines_by_scenario(self):
		mean_line = []
		num_lines = self.dataframes[0].shape[0]

		for j in range(num_lines):
			actual_sum = 0
			list_aux_by_scenario = []
			for i in range(self.n):
				self.dataframes[i].iloc[j] = self.dataframes[i].iloc[j].replace(',', '.').astype(float)
				sum = self.dataframes[i].iloc[j].sum()
				actual_sum = actual_sum + float(sum)
				list_aux_by_scenario.append(sum)
			self.list_times = list_aux_by_scenario
			values = self.calculate_stats_by_scenarios()
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
	def sum_columns_by_scenario(self):
		num_columns = self.dataframes[0].shape[1]
		total = {i: 0 for i in range(num_columns)}
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
	def save_results_pdf(self, file_name, file_name_img):
		# Calculate the values to save in the PDF.
		self.calculate_stats_by_execution()

		pdf = FPDF()

		# Add a page
		pdf.add_page()

		# set style and size of font that you want in the pdf
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
		# Define the size of the figure
		fig, ax = plt.subplots(figsize=(8, 3))

		# Assign the data
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
		vp = ax.violinplot(data_x, points=500, showmeans=False, showextrema=False, showmedians=False, vert=False)

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

		# Define cofigs for the plot
		plt.subplots_adjust(left=0.25)
		plt.yticks(np.arange(1,2), ['Means of the '+variable])  # Set text labels.
		plt.subplots_adjust(bottom=0.25)
		plt.xlabel('Values (seconds)')
		plt.title("Results by "+variable)
		plt.savefig("rain_cloud_"+variable+"_time.jpg", dpi=300)

		self.save_results_pdf("rain_cloud_" + variable + "_time.pdf", "rain_cloud_" + variable + "_time.jpg")

	# Create sheets by scenario
	def create_sheet_by_scenario(self):
		columns_sheet_by_scenario = [str("Execution-"+str(i+1)) for i in range(self.n)]+['mean', 'median', 'stardard deviation']
		df = pd.DataFrame(self.time_by_scenario, columns=columns_sheet_by_scenario)
		df = df.astype(float)
		df = df.round(2)

		print("Saving", 'output/results/sheets/results_by_scenario_all_execution.csv')
		df.to_csv('results_by_scenario_all_execution.csv', index=False)

# Check if n was passed as a parameter, otherwise it will default to 10
n = 10
if len(sys.argv) > 1:
	n = int(sys.argv[1])
else:
	n = 10

print("Running with n =", n)

ResultAnalysis(n)