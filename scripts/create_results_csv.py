import sys
import csv

soot_results_csv = sys.argv[1]  # soot results csv file
merge_dataset_csv = sys.argv[2]  # merge dataset csv file
results_with_build_info_csv = sys.argv[3]  # results with build info csv

COLON = ","
SEMI_COLON = ";"

RESULTS_FILE_NAME = "static-analysis-results.csv"

BLANK_FIELDS = ["Changes by Left", "Changes by Right",
                "KDiff Screenshot", "DF Exist", "CF Exist", "OA Exist", "Researcher"]


def main():
    # skiping comment regarding soot version
    soot_results = read_csv(soot_results_csv, SEMI_COLON)

    merge_dataset = read_csv(merge_dataset_csv, COLON)
    result_with_build_info = read_csv(results_with_build_info_csv, SEMI_COLON)

    def compare_merge_dataset_soot_results(a, b):
        return a["merge commit"] == b["Commit"] and a["class"] == b["Class"] and a["method"] == b["Declaration"].replace("|", ",")

    def compare_soot_results_result_with_build_info(a, b):
        return a["merge commit"] == b["merge commit"] and a["method"] == b["method"] and a["class"] == b["className"]

    merged_results = left_join_on(
        left_join_on(
            soot_results, merge_dataset, compare_merge_dataset_soot_results),
        result_with_build_info,
        compare_soot_results_result_with_build_info
    )

    write_csv("static-analysis-results.csv", ";", list(
        map(lambda x: add_blank_fields(include_wanted_fields(x), BLANK_FIELDS), merged_results)))


def add_blank_fields(scenario, blank_fields):
    result = scenario.copy()
    for field in blank_fields:
        result[field] = ""
    return result


def include_wanted_fields(scenario):
    return {
        "Project": scenario["project"],
        "Commit": scenario["merge commit"],
        "ClassName": scenario["class"],
        "Method": scenario["method"],
        "left right DF Intra": scenario.get("left right DF Intra", ""),
        "right left DF Intra": scenario.get("right left DF Intra", ""),
        "left right DF Inter": scenario.get("left right DF Inter", ""),
        "right left DF Inter": scenario.get("right left DF Inter", ""),
        "OA Intra": scenario.get("OA Intra", ""),
        "Confluence Intra": scenario.get("Confluence Intra", ""),
        "Confluence Inter": scenario.get("Confluence Inter", ""),
        "Sample": scenario.get("Sample", ""),
        "Locally Observable Interference": scenario.get("Locally Observable Interference", "") if scenario.get("Manually Analyzed", "") == "Yes" else "",
        "Left Changed Lines": scenario["left modifications"],
        "Right Changed Lines": scenario["right modifications"],
    }


def left_join_on(listA, listB, compareFn):
    return map(lambda val: find_corresponding_in_other_list_and_merge_dicts(val, listB, compareFn), listA)


def find_corresponding_in_other_list_and_merge_dicts(val, listB, compareFn):
    correspoding_in_list_b = list(filter(lambda x: compareFn(val, x), listB))

    if len(correspoding_in_list_b):
        return {**val, **correspoding_in_list_b[0]}
    return val


def read_csv(file_path, delimiter):
    with open(file_path, "r") as input_lines:
        return list(csv.DictReader(input_lines, delimiter=delimiter,))


def write_csv(file_path, delimiter, data):
    if data:
        with open(file_path, 'w') as outputFile:
            csv_writer = csv.DictWriter(
                outputFile, fieldnames=data[0].keys(), delimiter=delimiter)

            csv_writer.writeheader()
            for val in data:
                csv_writer.writerow(val)


if __name__ == "__main__":
    main()
