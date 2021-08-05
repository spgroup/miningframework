package services.dataCollectors.S3MWithCSDiffCollector

import project.MergeCommit
import project.Project
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.util.Utils
import util.ProcessRunner

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ParseProblemException
import com.github.javaparser.StaticJavaParser

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class FilesQuadruplesCollector {

    static List<Path> collectFilesQuadruples(Project project, MergeCommit mergeCommit) {
        String commitSHA = mergeCommit.getLeftSHA()
        List<String> leftFilePaths = MutuallyModifiedFilesCommitFilter.getModifiedJavaFilePaths(project, mergeCommit, commitSHA)

        commitSHA = mergeCommit.getRightSHA()
        List<String> rightFilePaths = MutuallyModifiedFilesCommitFilter.getModifiedJavaFilePaths(project, mergeCommit, commitSHA)

        List<String> mutuallyModifiedFilePaths = leftFilePaths.intersect(rightFilePaths)
        return mutuallyModifiedFilePaths.stream()
            .map(filePath -> FilesQuadruplesCollector::saveFilesQuadruple(project, mergeCommit, filePath))
            .collect(Collectors.toList())
    }

    private static Path saveFilesQuadruple(Project project, MergeCommit mergeCommit, String filePath) {
        Path filesQuadruplePath = Utils.commitFilesPath(project, mergeCommit).resolve(filePath)
        filesQuadruplePath.toFile().mkdirs()

        for (String fileName: [ 'left', 'base', 'right', 'merge' ]) {
            String commitSHA = getCommitSHA(mergeCommit, fileName)
            String fileContent = getFileContent(project, commitSHA, filePath)

            try {
                fileContent = removeComments(fileContent)
                saveFile(filesQuadruplePath, fileName, fileContent)
            } catch (ParseProblemException e) {
                println "Couldn't parse ${fileName} file in ${filePath}"
                saveFile(filesQuadruplePath, fileName, fileContent)
            }
        }

        return filesQuadruplePath
    }

    private static String getCommitSHA(MergeCommit mergeCommit, String fileName) {
        switch (fileName) {
            case 'left':
                return mergeCommit.getLeftSHA()
            case 'base':
                return mergeCommit.getAncestorSHA()
            case 'right':
                return mergeCommit.getRightSHA()
            default:
                return mergeCommit.getSHA()
        }
    }

    private static String getFileContent(Project project, String commitSHA, String filePath) {
        StringBuilder fileContent = new StringBuilder()

        Process process = ProcessRunner.runProcess(project.getPath(), "git", "show", "${commitSHA}:${filePath}")
        process.getInputStream().eachLine {
            fileContent.append(it).append('\n')
        }

        return fileContent.toString()
    }
	
    private static String removeComments(String fileContent) throws ParseProblemException {
        StaticJavaParser.getConfiguration().setAttributeComments(false)
        CompilationUnit compilationUnit = StaticJavaParser.parse(fileContent)
        return compilationUnit.toString()
    }

    private static void saveFile(Path filesQuadruplePath, String fileName, String fileContent) {
        Path filePath = filesQuadruplePath.resolve("${fileName}.java")
        Files.deleteIfExists(filePath)
        filePath.toFile() << fileContent
    }

}