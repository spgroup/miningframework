package services.dataCollectors.S3MWithCSDiffCollector

import util.ProcessRunner
import util.TextualMergeStrategy

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class S3MRunner {

    static final Path S3M_PATH = Paths.get('dependencies/s3m.jar')

    /**
     * Runs S3M for each merge scenario and each textual merge strategy. Stores the result at the same directory
     * the merge scenario is located, using a subdirectory for each textual merge strategy.
     *
     * @param mergeScenarios
     * @param textualMergeStrategies
     */
    static void collectS3MResults(List<Path> mergeScenarios, List<TextualMergeStrategy> textualMergeStrategies) {
        mergeScenarios.parallelStream().forEach(
            mergeScenario -> runDifferentStrategies(mergeScenario, textualMergeStrategies)
        )
    }

    private static void runDifferentStrategies(Path mergeScenario, List<TextualMergeStrategy> textualMergeStrategies) {
        Path leftFile = getInvolvedFile(mergeScenario, 'left')
        Path baseFile = getInvolvedFile(mergeScenario, 'base')
        Path rightFile = getInvolvedFile(mergeScenario, 'right')

        for (TextualMergeStrategy textualMergeStrategy: textualMergeStrategies) {
            runS3M(leftFile, baseFile, rightFile, 'merge.java', textualMergeStrategy)
        }
    }

    private static Path getInvolvedFile(Path mergeScenario, String fileName) {
        return mergeScenario.resolve("${fileName}.java").toAbsolutePath()
    }

    private static void runS3M(Path leftFile, Path baseFile, Path rightFile, String outputFileName, TextualMergeStrategy textualMergeStrategy) {
        ProcessBuilder processBuilder = buildS3MProcess(leftFile, baseFile, rightFile, outputFileName, textualMergeStrategy)
        Process process = ProcessRunner.startProcess(processBuilder)
        process.getInputStream().eachLine{}
        process.waitFor()

        renameUnstructuredMergeFile(baseFile.getParent(), textualMergeStrategy, outputFileName)
    }

    private static ProcessBuilder buildS3MProcess(Path leftFile, Path baseFile, Path rightFile, String outputFileName, TextualMergeStrategy textualMergeStrategy) {
        ProcessBuilder processBuilder = ProcessRunner.buildProcess(getParentAsString(S3M_PATH))
        List<String> parameters = buildS3MParameters(leftFile, baseFile, rightFile, outputFileName, textualMergeStrategy)

        processBuilder.command().addAll(parameters)
        return processBuilder
    }

    private static List<String> buildS3MParameters(Path leftFile, Path baseFile, Path rightFile, String outputFileName, TextualMergeStrategy textualMergeStrategy) {
        List<String> parameters = ['java', '-jar', getFileNameAsString(S3M_PATH)]
        parameters.addAll(leftFile.toString(), baseFile.toString(), rightFile.toString())

        Path outputPath = getOutputPath(baseFile.getParent(), textualMergeStrategy, outputFileName)
        parameters.addAll('-o', outputPath.toString())

        parameters.addAll('-c', 'false', '-u', '-l', 'false')
        parameters.addAll('-tms', textualMergeStrategy.getCommandLineOption())

        return parameters
    }

    private static String getParentAsString(Path path) {
        return path.getParent().toString()
    }

    private static String getFileNameAsString(Path path) {
        return path.getFileName().toString()
    }

    private static Path getOutputPath(Path mergeScenario, TextualMergeStrategy textualMergeStrategy, String fileName) {
        return mergeScenario.resolve(textualMergeStrategy.name()).resolve(fileName)
    }

    private static void renameUnstructuredMergeFile(Path mergeScenario, TextualMergeStrategy textualMergeStrategy, String outputFileName) {
        String textualMergeStrategyName = textualMergeStrategy.name()
        Path currentUnstructuredMergeFile = mergeScenario.resolve(textualMergeStrategyName).resolve("${outputFileName}.merge")
        Path renamedUnstructuredMergeFile = mergeScenario.resolve("textual.java")
        Files.move(currentUnstructuredMergeFile, renamedUnstructuredMergeFile, StandardCopyOption.REPLACE_EXISTING)
    }

}