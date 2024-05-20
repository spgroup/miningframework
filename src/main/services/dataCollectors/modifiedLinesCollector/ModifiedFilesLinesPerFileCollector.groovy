package services.dataCollectors.modifiedLinesCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import util.FileManager
import services.dataCollectors.RevisionsFilesCollector
import util.TypeNameHelper

import static app.MiningFramework.arguments


/**
 * @requires: that a diffj cli is in the dependencies folder and that diff (textual diff tool) is installed
 * @provides: a [outputPath]/data/results.csv file with the following format:
 * project;merge commit;file path;left additions;left deletions;left changes;right additions;right deletions;right changes;repo url
 */
class ModifiedFilesLinesPerFileCollector extends ModifiedLinesCollectorAbstract {

    public ModifiedFilesLinesPerFileCollector() {
        modifiedMethodsHelper = new ModifiedMethodsHelper("diffj.jar");
    }

    void collectData(Project project, MergeCommit mergeCommit) {
        createOutputFiles(arguments.getOutputPath())
        Set<String> mutuallyModifiedFiles = getFilesModifiedByBothParents(project, mergeCommit);
        println "${project.getName()} - ModifiedFilesLinesCollector collection ongoing with mutuallyModifiedFiles: ${mutuallyModifiedFiles}"

        for (String filePath : mutuallyModifiedFiles) {
            Set<ModifiedLine> leftModifiedLines = modifiedMethodsHelper.getModifiedLines(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA())
            Set<ModifiedLine> rightModifiedLines = modifiedMethodsHelper.getModifiedLines(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA())
            
            collectFileData(leftModifiedLines, rightModifiedLines, project, mergeCommit, filePath)
        }
        println "${project.getName()} - ModifiedFilesLinesCollector collection finished"
    }

    void createExperimentalDataFiles(String outputPath) {
        this.experimentalDataFile = new File(outputPath + "/data/results.csv")
        if (!experimentalDataFile.exists()) {
            this.experimentalDataFile << 'project;merge commit;file path;left additions;left deletions;left changes;right additions;right deletions;right changes;repo url\n'
        }

        if (arguments.isPushCommandActive()) {
            this.experimentalDataFileWithLinks = new File("${outputPath}/data/result-links.csv");
        }
    }

    private synchronized void printResults(Project project, MergeCommit mergeCommit, String filePath,
                      HashSet<Integer> leftAddedLines, HashSet<Integer> leftDeletedLines, HashSet<Integer> leftChangedLines,
                      HashSet<Integer> rightAddedLines, HashSet<Integer> rightDeletedLines, HashSet<Integer> rightChangedLines) {
        String remoteRepositoryURL = project.getRemoteUrl()
        experimentalDataFile << "${project.getName()};${mergeCommit.getSHA()};${filePath};${leftAddedLines};${leftDeletedLines};${leftChangedLines};${rightAddedLines};${rightDeletedLines};${rightChangedLines};${remoteRepositoryURL}\n"

        // Add links.
        if(arguments.isPushCommandActive())
            addLinks(project.getName(), mergeCommit.getSHA(), filePath, leftAddedLines, leftDeletedLines, leftChangedLines, rightAddedLines, rightDeletedLines, rightChangedLines, arguments.getResultsRemoteRepositoryURL())

    }

}