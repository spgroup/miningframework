package services.dataCollectors.csDiffCollector

import project.MergeCommit
import project.Project
import services.outputProcessors.S3MOutputProcessor
import services.util.CSDiffMergeCommitSummary
import services.util.CSDiffMergeScenarioSummary
import services.util.Utils

import java.nio.file.Path

class SpreadsheetBuilder {
    private static final String GLOBAL_SPREADSHEET_HEADER = 'project,merge commit,number of files modified for both branches,number of Merge files with conflicts,number of CSDiff files with conflicts, number of git merge files with conflicts,number of Merge conflicts,number of CSDiff conflicts,number of git merge conflicts,CSDiff and [diff3 -E -m] have the same outputs,actual merge and textual (diff3 -E -m) have same output,actual merge and csdiff have same output,actual merge and git merge have same output,notes,,,'
    private static final String COMMIT_SPREADSHEET_HEADER = 'project,merge commit,file,number of Merge conflicts,number of CSDiff conflicts,Merge text = CSDiff text,Textual Text = Actual Merge Text,CSDiff Text = Actual Merge Text,Git Merge Text = Actual Merge Text'
    private static final String SPREADSHEET_NAME = 'results.csv'

    /**
     * Builds a global spreadsheet, based on the merge commit's summary, and a local spreadsheet, for each merge commit, based on
     * the merge scenario's summary.
     * @param project
     * @param mergeCommit
     * @param summary
     */
    static synchronized void buildSpreadsheets(Project project, MergeCommit mergeCommit, CSDiffMergeCommitSummary summary) {
        buildGlobalSpreadsheet(project, mergeCommit, summary)
        buildCommitSpreadsheet(project, mergeCommit, summary.mergeScenarioSummaries)
    }

    private static void buildCommitSpreadsheet(Project project, MergeCommit mergeCommit, List<CSDiffMergeScenarioSummary> summaries) {
        Path spreadsheetPath = Utils.commitFilesPath(project, mergeCommit).resolve(SPREADSHEET_NAME)
        File spreadsheet = spreadsheetPath.toFile()
        appendHeader(spreadsheet, COMMIT_SPREADSHEET_HEADER)

        summaries.each { summary ->
            appendLineToSpreadsheet(spreadsheet, appendAfterProjectAndMergeCommitLinks(project, mergeCommit, summary.toString()))
        }
    }

    private static void buildGlobalSpreadsheet(Project project, MergeCommit mergeCommit, CSDiffMergeCommitSummary summary) {
        Path spreadsheetPath = Utils.getOutputPath().resolve(SPREADSHEET_NAME)
        File spreadsheet = spreadsheetPath.toFile()
        appendHeader(spreadsheet, GLOBAL_SPREADSHEET_HEADER)

        appendLineToSpreadsheet(spreadsheet, appendAfterProjectAndMergeCommitLinks(project, mergeCommit, summary.toString()))
    }

    private static void appendHeader(File spreadsheet, String header) {
        if (!spreadsheet.exists()) {
            appendLineToSpreadsheet(spreadsheet, header)
        }
    }

    private static String appendAfterProjectAndMergeCommitLinks(Project project, MergeCommit mergeCommit, String string) {
        String projectName = project.getName()
        String commitSHA = mergeCommit.getSHA()
        return "${projectName},${commitSHA},${string}"
    }

    private static void appendLineToSpreadsheet(File spreadsheet, String line) {
        spreadsheet << "${line.replaceAll('\\\\', '/')}\n"
    }

}
