#!/usr/bin/python
import fitz
import difflib
from fpdf import FPDF
import sys

n = 10

if len(sys.argv) > 1:
	n = int(sys.argv[1])
else:
	n = 10

pdf = FPDF()

# Add a page
pdf.add_page()

# set style and size of font
# that you want in the pdf
pdf.set_font("Arial", size = 15)

# Define the files names
file_names = ['./output/results/execution-'+str(i+1)+'/results.pdf' for i in range(n)]

# Read the first file
ref_doc = fitz.open(file_names[0])
ref_page_count = ref_doc.page_count

# Compare the others file with the first
for file_name in file_names[1:]:
	other_doc = fitz.open(file_name)
	other_page_count = other_doc.page_count
	# Check the number of the pages
	if ref_page_count != other_page_count:
		print(f"The file {file_name} has a different number of pages..")
		continue
	# Compara o conteúdo de cada página
	for page_num in range(ref_page_count):
		ref_page = ref_doc[page_num]
		other_page = other_doc[page_num]
		ref_text = ref_page.get_text("text")
		other_text = other_page.get_text("text")
		if ref_text != other_text:
			text = f"The file {file_name} has a difference on the page {page_num + 1}."
			print(text)
			pdf.cell(200, 10, txt = text, ln = 1, align = 'C')

			diff = difflib.ndiff(ref_text.splitlines(), other_text.splitlines())

			save_string = '\n'.join(list(diff))
			for st in save_string.split("\n"):
				pdf.cell(200, 10, txt = st, ln = 1, align = 'C')
	other_doc.close()

# save the pdf
pdf.output("diff_files.pdf")
print("Saving the differences of the results in", "diff_files.pdf")

ref_doc.close()

