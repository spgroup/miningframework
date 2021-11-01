package services.dataCollectors.S3MMergesCollector

import project.MergeCommit
import project.Project
import util.ProcessRunner
import services.util.Utils

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

/**
 * Class responsible for collecting and storing eligible merge scenarios (modified from base java files).
 */
class MergeScenarioCollector {

    /**
     * Stores merge scenarios (left, base, right and merge files) encountered in the merge commit.
     * @param project
     * @param mergeCommit
     * @return a list of directory paths where each merge scenario is located
     */
    static List<Path> collectMergeScenarios(Project project, MergeCommit mergeCommit) {
        return getModifiedJavaFiles(project, mergeCommit).stream()
                .map(modifiedFile -> storeAndRetrieveMergeQuadruple(project, mergeCommit, modifiedFile))
                .map(quadruple -> quadruple.getV4().getParent())
                .collect(Collectors.toList())
    }

    private static Tuple4<Path, Path, Path, Path> storeAndRetrieveMergeQuadruple(Project project, MergeCommit mergeCommit, String modifiedFile) {
        Path leftFile = storeFile(project, mergeCommit, modifiedFile, mergeCommit.getLeftSHA(), 'left')
        Path baseFile = storeFile(project, mergeCommit, modifiedFile, mergeCommit.getAncestorSHA(), 'base')
        Path rightFile = storeFile(project, mergeCommit, modifiedFile, mergeCommit.getRightSHA(), 'right')
        Path mergeFile = storeFile(project, mergeCommit, modifiedFile, mergeCommit.getSHA(), 'merge')
        return new Tuple4(leftFile, baseFile, rightFile, mergeFile)
    }

    private static Path storeFile(Project project, MergeCommit mergeCommit, String modifiedFile, String commitSHA, String fileName) {
        Path mergeScenarioDirectory = Utils.commitFilesPath(project, mergeCommit).resolve(modifiedFile)
        createDirectories(mergeScenarioDirectory)

        Path filePath = mergeScenarioDirectory.resolve(Utils.getfileNameWithExtension(fileName))
        Files.deleteIfExists(filePath)
        filePath.toFile() << getFileContent(project, modifiedFile, commitSHA)
        return filePath
    }

    private static String getFileContent(Project project, String modifiedFile, String commitSHA) {
        StringBuilder fileContent = new StringBuilder()

        Process gitShow = ProcessRunner.runProcess(project.getPath(), "git", "show", "${commitSHA}:${modifiedFile}")
        gitShow.getInputStream().eachLine {
            fileContent.append(it).append('\n')
        }
        return fileContent.toString()
    }

    private static List<String> getModifiedJavaFiles(Project project, MergeCommit mergeCommit) {
        Process gitDiffTree = ProcessRunner.runProcess(project.getPath(), "git", "diff-tree", "--no-commit-id", "--name-status", "-r", mergeCommit.getSHA(), mergeCommit.getAncestorSHA())
        List<String> modifiedFiles = gitDiffTree.getInputStream().readLines()

        return modifiedFiles.stream()
                .filter(MergeScenarioCollector::isModifiedFile)
                .filter(MergeScenarioCollector::isRequiredFileFormat)
                .map(MergeScenarioCollector::getPath)
                .collect(Collectors.toList())
    }

    private static boolean isModifiedFile(String line) {
        return line.charAt(0) == 'M' as char
    }

    private static boolean isRequiredFileFormat(String line) {
        line.endsWith(arguments.getFileExtension())
    }

    private static String getPath(String line) {
        return line.substring(1).trim()
    }

    private static void createDirectories(Path path) {
        path.toFile().mkdirs()
    }

}
