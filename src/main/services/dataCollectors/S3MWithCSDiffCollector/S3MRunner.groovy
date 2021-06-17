package services.dataCollectors.S3MWithCSDiffCollector

import services.util.MergeToolRunner
import util.ProcessRunner
import util.TextualMergeStrategy

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class S3MRunner extends MergeToolRunner {

    private static final Path S3M_PATH = Paths.get('dependencies/s3m.jar')

    S3MRunner(String mergeToolName) {
        this.mergeToolName = mergeToolName
    }

    protected void runTool(Path leftFile, Path baseFile, Path rightFile) {
        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            runS3M(leftFile, baseFile, rightFile, strategy)
        }
    }

    private void runS3M(Path leftFile, Path baseFile, Path rightFile, TextualMergeStrategy strategy) {
        ProcessBuilder processBuilder = buildProcess(leftFile, baseFile, rightFile, strategy)
        runProcess(processBuilder)
    }

    private ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile, TextualMergeStrategy strategy) {
        String processDirectory = S3M_PATH.getParent().toString()
        ProcessBuilder processBuilder = ProcessRunner.buildProcess(processDirectory)
        List<String> parameters = buildParameters(leftFile, baseFile, rightFile, strategy)

        processBuilder.command().addAll(parameters)
        return processBuilder
    }

    private List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile, TextualMergeStrategy strategy) {
        List<String> parameters = ['java', '-jar', S3M_PATH.getFileName().toString()]
        parameters.addAll(leftFile.toString(), baseFile.toString(), rightFile.toString())

        Path outputPath = getOutputPath(baseFile.getParent(), strategy.name())
        parameters.addAll('-o', outputPath.toString())

        parameters.addAll('-c', 'false', '-l', 'false')
        parameters.addAll('-tms', strategy.getCommandLineOption())

        return parameters
    }

}