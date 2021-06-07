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
        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            headers.add("number of ${strategy.name()} conflicts")
        }

        headers.addAll('strategies have same outputs', 'strategies have same conflicts')
        return headers
    }

    private static List<String> getCommitSpreadsheetHeaders() {
        List<String> headers = [ 'project', 'merge commit', 'file' ]
        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            headers.add("number of ${strategy.name()} conflicts")
        }

        for (int i = 0; i < MergesCollector.strategies.size(); i++) {
            TextualMergeStrategy strategy1 = MergesCollector.strategies[i]
            for (int j = i + 1; j < MergesCollector.strategies.size(); j++) {
                TextualMergeStrategy strategy2 = MergesCollector.strategies[j]
                headers.add("${strategy1.name()} output = ${strategy2.name()} output")
            }
        }

        for (int i = 0; i < MergesCollector.strategies.size(); i++) {
            TextualMergeStrategy strategy1 = MergesCollector.strategies[i]
            for (int j = i + 1; j < MergesCollector.strategies.size(); j++) {
                TextualMergeStrategy strategy2 = MergesCollector.strategies[j]
                headers.add("${strategy1.name()} conflicts = ${strategy2.name()} conflicts")
            }
        }

        return headers
    }

    private static String getHeaderLine(List<String> headers) {
        return headers.join(',')
    }

}