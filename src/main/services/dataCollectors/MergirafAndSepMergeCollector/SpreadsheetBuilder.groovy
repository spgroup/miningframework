package services.dataCollectors.MergirafAndSepMergeCollector

import project.MergeCommit
import project.Project
import services.util.Utils

import java.nio.file.Path

class SpreadsheetBuilder {
    private static final String SEPARATOR = ','
    private static final String SPREADSHEET_NAME = 'results.csv'
    private static final Map<String, Closure> HEADER_NAME_TO_VALUE_GETTER

    static {
        HEADER_NAME_TO_VALUE_GETTER = [
            project: { project, _, __ -> project.getName() },
            'merge commit': { _, mergeCommit, __ -> mergeCommit.getSHA() },
            file: { _, __, mergeSummary -> mergeSummary.getFileName() }
        ]

        List<String> mergeIds = MergeSummary.getMergeIds()
        for (String mergeId: mergeIds) {
            String currentMergeId = mergeId
            String headerName = "number of ${currentMergeId} conflicts"

            HEADER_NAME_TO_VALUE_GETTER[headerName] = { _, __, mergeSummary ->
                Integer.toString(mergeSummary.getNumberOfConflicts(currentMergeId))
            }
        }

        for (int i = 0; i < mergeIds.size(); i++) {
            String firstMergeId = mergeIds[i]
            for (int j = i + 1; j < mergeIds.size(); j++) {
                String secondMergeId = mergeIds[j]
                String headerName = "${firstMergeId} content = ${secondMergeId} content"

                HEADER_NAME_TO_VALUE_GETTER[headerName] = { _, __, mergeSummary ->
                    Boolean.toString(mergeSummary.mergesHaveSameContents(firstMergeId, secondMergeId))
                }
            }
        }

        for (int i = 0; i < mergeIds.size(); i++) {
            String firstMergeId = mergeIds[i]
            for (int j = i + 1; j < mergeIds.size(); j++) {
                String secondMergeId = mergeIds[j]
                String headerName = "${firstMergeId} conflicts = ${secondMergeId} conflicts"

                HEADER_NAME_TO_VALUE_GETTER[headerName] = { _, __, mergeSummary ->
                    Boolean.toString(mergeSummary.mergesHaveSameConflicts(firstMergeId, secondMergeId))
                }
            }
        }
    }

    static synchronized void updateSpreadsheet(Project project, MergeCommit mergeCommit, List<MergeSummary> summaries) {
        File spreadsheet = Utils.getOutputPath().resolve(SPREADSHEET_NAME).toFile()
        if (!spreadsheet.exists()) {
            String headerLine = getSpreadsheetHeaderLine()
            appendLineToSpreadsheet(spreadsheet, headerLine)
        }

        summaries.each { summary ->
            String valuesLine = getSpreadsheetValuesLine(project, mergeCommit, summary)
            appendLineToSpreadsheet(spreadsheet, valuesLine)
        }
    }

    private static String getSpreadsheetHeaderLine() {
        List<String> headerNames = HEADER_NAME_TO_VALUE_GETTER.keySet().toList()
        return headerNames.join(SEPARATOR)
    }

    private static String getSpreadsheetValuesLine(Project project, MergeCommit mergeCommit, MergeSummary mergeSummary) {
        List<String> values = []
        HEADER_NAME_TO_VALUE_GETTER.each { _, getValue ->
            values.add(getValue(project, mergeCommit, mergeSummary))
        }

        return values.join(SEPARATOR)
    }

    private static void appendLineToSpreadsheet(File spreadsheet, String line) {
        spreadsheet << "${line.replaceAll('\\\\', '/')}\n"
    }
}
