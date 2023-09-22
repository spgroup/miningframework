#!/usr/bin/python

import pandas as pd
import sys
import helper_functions as func

# Check if n was passed as a parameter, otherwise it will default to 10
n = 10
if len(sys.argv) > 1:
    n = int(sys.argv[1])
else:
    n = 10

# generating a CSV file of times for the Nth execution
for i in range(n):
    func.generating_sheets_result(str(i + 1), "./output/results/execution-", "time", "resultTime", ";")
