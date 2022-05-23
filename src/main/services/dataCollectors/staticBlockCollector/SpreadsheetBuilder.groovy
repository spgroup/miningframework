package services.dataCollectors.staticBlockCollector

import java.nio.file.Path

import project.MergeCommit
import project.Project
import services.util.Utils


class SpreadsheetBuilder {

    private static final String SPREADSHEET_NAME = 'staticBlockResults.csv'

    static synchronized void updateSpreadsheet(Project project, MergeCommit mergeCommit, List<MergeSummary> summaries) {
        File spreadsheet = Utils.getOutputPath().resolve(SPREADSHEET_NAME).toFile()
        if (!spreadsheet.exists()) {
            String headerLine = getSpreadsheetHeaderLine()
            appendLineToSpreadsheet(spreadsheet, headerLine)
        }

        summaries.each { summary ->
            String newLine = "${project.getName()},${mergeCommit.getSHA()},${summary.toString()}"
            appendLineToSpreadsheet(spreadsheet, newLine)
        }
    }

    private static String getSpreadsheetHeaderLine() {
        List<String> headers = [ 'project', 'merge commit', 'file' ]

        List<String> mergeApproaches = MergesCollector.getMergeApproaches()
        for (String approach: mergeApproaches) {
            headers.add("number of ${approach} conflicts")
        }

        for (int i = 0; i < mergeApproaches.size(); i++) {
            String approach1 = mergeApproaches[i]
            for (int j = i + 1; j < mergeApproaches.size(); j++) {
                String approach2 = mergeApproaches[j]
                headers.add("${approach1} output = ${approach2} output")
            }
        }

        for (int i = 0; i < mergeApproaches.size(); i++) {
            String approach1 = mergeApproaches[i]
            for (int j = i + 1; j < mergeApproaches.size(); j++) {
                String approach2 = mergeApproaches[j]
                headers.add("${approach1} conflicts = ${approach2} conflicts")
            }
        }

        return headers.join(',')
    }

    private static void appendLineToSpreadsheet(File spreadsheet, String line) {
        spreadsheet << "${line.replaceAll('\\\\', '/')}\n"
    }

}