package services.dataCollectors.S3MWithCSDiffCollector

import services.util.MergeToolRunner
import util.ProcessRunner
import util.TextualMergeStrategy

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class S3MRunner extends MergeToolRunner {

    private static final Path S3M_PATH = Paths.get('dependencies/s3m.jar')

    private TextualMergeStrategy strategy

    S3MRunner(TextualMergeStrategy strategy) {
        this.mergeToolName = 'S3M'
        this.strategy = strategy
    }

    protected ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile) {
        String processDirectory = S3M_PATH.getParent().toString()
        return ProcessRunner.buildProcess(processDirectory)
    }

    protected List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile) {
        String jarFileName = S3M_PATH.getFileName().toString()
        List<String> parameters = ['java', '-jar', jarFileName]
        parameters.addAll(leftFile.toString(), baseFile.toString(), rightFile.toString())

        String mergeFileName = strategy.name()
        Path filesQuadruplePath = baseFile.getParent()
        Path outputPath = getOutputPath(filesQuadruplePath, mergeFileName)
        parameters.addAll('-o', outputPath.toString())

        parameters.addAll('-c', 'false', '-l', 'false')
        parameters.addAll('-tms', strategy.getCommandLineOption())

        return parameters
    }

    protected void processOutput(Path filesQuadruplePath) {}

}