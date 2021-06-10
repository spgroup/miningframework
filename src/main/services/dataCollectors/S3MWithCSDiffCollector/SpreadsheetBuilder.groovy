package services.dataCollectors.S3MWithCSDiffCollector

import java.nio.file.Path

import project.MergeCommit
import project.Project
import services.dataCollectors.S3MWithCSDiffCollector.MergesCollector
import services.dataCollectors.S3MWithCSDiffCollector.MergeCommitSummary
import services.dataCollectors.S3MWithCSDiffCollector.MergeScenarioSummary
import services.util.Utils
import util.TextualMergeStrategy

class SpreadsheetBuilder {

    private static final String SPREADSHEET_NAME = "results.csv"

    static synchronized void buildSpreadsheets(Project project, MergeCommit mergeCommit, MergeCommitSummary summary) {
        updateGlobalSpreadsheet(project, mergeCommit, summary)
        buildCommitSpreadsheet(project, mergeCommit, summary)
    }

    private static void updateGlobalSpreadsheet(Project project, MergeCommit mergeCommit, MergeCommitSummary summary) {
        File spreadsheet = Utils.getOutputPath().resolve(SPREADSHEET_NAME).toFile()
        if (!spreadsheet.exists()) {
            List<String> headers = getGlobalSpreadsheetHeaders()
            String headerLine = getHeaderLine(headers)
            appendLineToSpreadsheet(spreadsheet, headerLine)
        }

        String newLine = "${project.getName()},${mergeCommit.getSHA()},${summary.toString()}"
        appendLineToSpreadsheet(spreadsheet, newLine)
    }

    private static void buildCommitSpreadsheet(Project project, MergeCommit mergeCommit, MergeCommitSummary summary) {
        File spreadsheet = Utils.commitFilesPath(project, mergeCommit).resolve(SPREADSHEET_NAME).toFile()

        List<String> headers = getCommitSpreadsheetHeaders()
        String headerLine = getHeaderLine(headers)
        appendLineToSpreadsheet(spreadsheet, headerLine)

        summary.mergeScenarioSummaries.each { mergeScenarioSummary ->
            String newLine = "${project.getName()},${mergeCommit.getSHA()},${mergeScenarioSummary.toString()}"
            appendLineToSpreadsheet(spreadsheet, newLine)
        }
    }

    private static void appendLineToSpreadsheet(File spreadsheet, String line) {
        spreadsheet << "${line.replaceAll('\\\\', '/')}\n"
    }

    private static List<String> getGlobalSpreadsheetHeaders() {
        List<String> headers = [ 'project', 'merge commit', 'number of modified files' ]

        List<String> mergeApproaches = MergesCollector.getMergeApproaches()
        for (String approach: mergeApproaches) {
            headers.add("number of ${approach} conflicts")
        }

        headers.addAll('approaches have same outputs', 'approaches have same conflicts')
        return headers
    }

    private static List<String> getCommitSpreadsheetHeaders() {
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

        return headers
    }

    private static String getHeaderLine(List<String> headers) {
        return headers.join(',')
    }

}