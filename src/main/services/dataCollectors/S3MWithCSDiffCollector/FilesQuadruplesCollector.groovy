package services.dataCollectors.S3MWithCSDiffCollector

import project.MergeCommit
import project.Project
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.util.Utils
import util.ProcessRunner

import com.github.javaparser.ast.CompilationUnit
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
		
		String leftContent = getFileContent(project, mergeCommit.getLeftSHA(), filePath)
		saveFile(filesQuadruplePath, 'left', removeComments(leftContent))
		
		String baseContent = getFileContent(project, mergeCommit.getAncestorSHA(), filePath)
		saveFile(filesQuadruplePath, 'base', removeComments(baseContent))
		
		String rightContent = getFileContent(project, mergeCommit.getRightSHA(), filePath)
		saveFile(filesQuadruplePath, 'right', removeComments(rightContent))
		
		String mergeContent = getFileContent(project, mergeCommit.getSHA(), filePath)
		saveFile(filesQuadruplePath, 'merge', removeComments(mergeContent))

        return filesQuadruplePath
    }

    private static String getFileContent(Project project, String commitSHA, String filePath) {
        StringBuilder fileContent = new StringBuilder()

        Process process = ProcessRunner.runProcess(project.getPath(), "git", "show", "${commitSHA}:${filePath}")
        process.getInputStream().eachLine {
            fileContent.append(it).append('\n')
        }

        return fileContent.toString()
    }
	
	private static String removeComments(String content) {
		StaticJavaParser.getConfiguration().setAttributeComments(false)
		CompilationUnit compilationUnit = StaticJavaParser.parse(content)
		return compilationUnit.toString()
	}

    private static void saveFile(Path filesQuadruplePath, String fileName, String fileContent) {
        Path filePath = filesQuadruplePath.resolve("${fileName}.java")
        Files.deleteIfExists(filePath)
        filePath.toFile() << fileContent
    }

}