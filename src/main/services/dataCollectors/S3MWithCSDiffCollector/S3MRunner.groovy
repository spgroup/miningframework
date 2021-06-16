package services.dataCollectors.S3MWithCSDiffCollector

import services.util.MergeToolRunner
import util.ProcessRunner
import util.TextualMergeStrategy

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class S3MRunner extends MergeToolRunner {

    private static final String MERGE_FILE_NAME = 'merge.java'
    private static final Path S3M_PATH = Paths.get('dependencies/s3m.jar')

    /**
     * Runs S3M for each merge scenario and each textual merge strategy. Stores the result at the same directory
     * the merge scenario is located, using a subdirectory for each textual merge strategy.
     *
     * @param filesQuadruplePaths
     * @param textualMergeStrategies
     */
    void collectResults(List<Path> filesQuadruplePaths) {
        filesQuadruplePaths.each { filesQuadruplePath ->
            runDifferentStrategies(filesQuadruplePath)
        }
    }

    private void runDifferentStrategies(Path filesQuadruplePath) {
        Path leftFile = getContributionFile(filesQuadruplePath, 'left')
        Path baseFile = getContributionFile(filesQuadruplePath, 'base')
        Path rightFile = getContributionFile(filesQuadruplePath, 'right')

        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            runS3M(leftFile, baseFile, rightFile, strategy)
        }
    }

    private void runS3M(Path leftFile, Path baseFile, Path rightFile, TextualMergeStrategy strategy) {
        ProcessBuilder processBuilder = buildS3MProcess(leftFile, baseFile, rightFile, strategy)
        Process process = ProcessRunner.startProcess(processBuilder)
        process.getInputStream().eachLine{}
        process.waitFor()
    }

    private ProcessBuilder buildS3MProcess(Path leftFile, Path baseFile, Path rightFile, TextualMergeStrategy strategy) {
        ProcessBuilder processBuilder = ProcessRunner.buildProcess(getParentAsString(S3M_PATH))
        List<String> parameters = buildS3MParameters(leftFile, baseFile, rightFile, strategy)

        processBuilder.command().addAll(parameters)
        return processBuilder
    }

    private List<String> buildS3MParameters(Path leftFile, Path baseFile, Path rightFile, TextualMergeStrategy strategy) {
        List<String> parameters = ['java', '-jar', getFileNameAsString(S3M_PATH)]
        parameters.addAll(leftFile.toString(), baseFile.toString(), rightFile.toString())

        Path outputPath = getOutputPath(baseFile.getParent(), strategy)
        parameters.addAll('-o', outputPath.toString())

        parameters.addAll('-c', 'false', '-l', 'false')
        parameters.addAll('-tms', strategy.getCommandLineOption())

        return parameters
    }

    private String getParentAsString(Path path) {
        return path.getParent().toString()
    }

    private String getFileNameAsString(Path path) {
        return path.getFileName().toString()
    }

    private Path getOutputPath(Path filesQuadruplePath, TextualMergeStrategy strategy) {
        return filesQuadruplePath.resolve(strategy.name()).resolve(MERGE_FILE_NAME)
    }

}