import sys

CLASS_NAME = "className"
LEFT_MODIFICATION = "leftModification"
RIGHT_MODIFICATION = "rightModfication"
COMMIT_SHA = "commitSha"
PROJECT_NAME = "projectName"

outputPath = sys.argv[1] # get output path passed as cli argument
def exportCsv():
    f = open(outputPath + "/data/results.csv", "r")
    file = f.read()
    f.close()

    bruteLines = file.split("\n")

    parsed = parse_output(bruteLines)
    csv = generate_csv(parsed)


def parse_output(lines):
    result = []
    for line in lines[1:]:
        cells = line.split(";")
        if (len (cells) > 1):
            method = {}
            method[PROJECT_NAME] = cells[0]
            method[COMMIT_SHA] = cells[1]
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
    for elem in collection:
        result = []
        className = elem[CLASS_NAME]
        leftMods = elem[LEFT_MODIFICATION]
        rightMods = elem[RIGHT_MODIFICATION]
        for l in leftMods:
            result.append(className + ",source," + l)
        for r in rightMods:
            result.append(className + ",sink," + r)
        try:
            csvFile = open(outputPath + "/files/" + elem[PROJECT_NAME] + "/" + elem[COMMIT_SHA] + "/soot.csv", "w")
            csvFile.write("\n".join(result))
            csvFile.close()
        except:
            pass

exportCsv()