package services.dataCollectors.staticBlockCollector

import app.MiningFramework

import java.nio.file.Path

import project.MergeCommit
import project.Project
import services.util.Utils

import java.nio.file.Paths

import static app.MiningFramework.arguments


class SpreadsheetBuilder {

    private static final String SPREADSHEET_NAME = 'staticBlockResults.csv'

    static final String FILTER_MERGES_SCENARIOS = "merges_scenarios.csv"
    static final String FILTER_BRANCHES_CHANGED_LEAST_ONE_COMMON_FILE = "branches_changed_least_one_common_file.csv"
    static final String FILTER_COMMON_FILES_INITIALIZATION_BLOCK_BOTH_BRANCHES = "common_files_iniatilization_block_both_branches.csv"
    static final String FILTER_BRANCHED_CHANGED_LEAST_ONE_INITIALIZATION_BLOCK = "branched_changed_least_on_iniatilization_block.csv"

    static synchronized void updateSpreadsheet(Project project, MergeCommit mergeCommit, List<MergeSummary> summaries) {
        File spreadsheet = Utils.getOutputPath().resolve(project.getName() + "_" + SPREADSHEET_NAME).toFile()
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
        List<String> headers = ['project', 'merge commit', 'file']

        List<String> mergeApproaches = MergesCollector.getMergeApproaches()
        for (String approach : mergeApproaches) {
            headers.add("number of ${approach} conflicts")
        }
        for (String approach : mergeApproaches) {
            headers.add("number of ${approach} for initialization block conflicts")
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

    static void obtainResultsSpreadsheetForProject(Project project,MergeCommit mergeCommit, String targetFile, int qtdStaticBlock) {
        Path path =  Paths.get(MiningFramework.arguments.getOutputPath() + '/data/')
        File spreadsheet = path.resolve(project.getName() + "_" + FILTER_BRANCHED_CHANGED_LEAST_ONE_INITIALIZATION_BLOCK).toFile()
        if (!spreadsheet.exists()) {
            spreadsheet << 'project; merge commit ;ancestorSHA; left; right; hasIniatializationBlock;  qtd_static\n'
        }
        spreadsheet << "${project.getName()};${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};${targetFile};${qtdStaticBlock}\n"
    }
    static void obtainResultsSpreadsheetForProject(Project project,MergeCommit mergeCommit, String name) {
        Path path = Paths.get(MiningFramework.arguments.getOutputPath() + '/data/')
        print"${path.toAbsolutePath().toString()}"
        File spreadsheet = path.resolve(project.getName() + "_" + name).toFile()
        print"#PLANILHA : ${spreadsheet.getAbsolutePath()} #\n"
        if (!spreadsheet.exists()) {
            spreadsheet << 'Merge commit; Ancestor; left; right;\n'
        }
        if(name.equals(FILTER_COMMON_FILES_INITIALIZATION_BLOCK_BOTH_BRANCHES) &&
                printMergeCommitInitializationBlock(mergeCommit, spreadsheet.getAbsolutePath())) {
            spreadsheet << "${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};\n"
        }else{
            spreadsheet << "${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};\n"
        }
    }

    private boolean printMergeCommitInitializationBlock( MergeCommit mergeCommit, String absolutePath){
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(absolutePath)))
        def line = null;
        while ((line = reader.readLine()) != null) {
            String str = line.split(";")[0]
            if(mergeCommit.getSHA().equals(str) ){
                return false
            }
        }
        return true;
    }
}