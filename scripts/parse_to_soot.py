import sys

CLASS_NAME = "className"
LEFT_MODIFICATION = "leftModification"
RIGHT_MODIFICATION = "rightModfication"

def exportCsv():
    outputPath = sys.argv[1] # get output path passed as cli argument
    f = open(outputPath + "/data/results.csv", "r")
    file = f.read()
    f.close()

    bruteLines = file.split("\n")

    parsed = parse_output(bruteLines)
    csv = generate_csv(parsed)

    csvFile = open(outputPath + "/data/results-soot.csv", "w")
    csvFile.write(csv)
    csvFile.close()

def parse_output(lines):
    result = []
    for line in lines[1:]:
        cells = line.split(";")
        if (len (cells) > 1):
            method = {}
            method[CLASS_NAME] = cells[2]
            method[LEFT_MODIFICATION] = parse_modification(cells[4])
            method[RIGHT_MODIFICATION] = parse_modification(cells[6])
            result.append(method)
    return result

def parse_modification(modifications):
    trimmedInput = modifications.strip("[]").replace(" ", "")
    if (len (trimmedInput) > 0):
        return trimmedInput.split(",")
    return []

def generate_csv(collection):
    result = []
    for elem in collection:
        className = elem[CLASS_NAME]
        leftMods = elem[LEFT_MODIFICATION]
        rightMods = elem[RIGHT_MODIFICATION]
        for l in leftMods:
            result.append(className + ",source," + l)
        for r in rightMods:
            result.append(className + ",sink," + r)
    return "\n".join(result)

exportCsv()