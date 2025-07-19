package services.dataCollectors.MergirafAndSepMergeCollector.mergeToolRunners

import services.util.MergeToolRunner
import util.ProcessRunner

import java.nio.file.Path
import java.nio.file.Paths

class MergirafRunner extends MergeToolRunner {
    private static final Path MERGIRAF_PATH = Paths.get('dependencies/mergiraf')

    MergirafRunner() {
        this.mergeToolName = 'mergiraf'
    }

    protected ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile) {
        String processDirectory = MERGIRAF_PATH.getParent().toString()
        return ProcessRunner.buildProcess(processDirectory)
    }

    protected List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile) {
        String binaryFileCommand = "./${MERGIRAF_PATH.getFileName().toString()}"
        List<String> parameters = [
            binaryFileCommand, 'merge',
            '-s', DEFAULT_BASE_MARKER_NAME,
            '-x', DEFAULT_LEFT_MARKER_NAME,
            '-y', DEFAULT_RIGHT_MARKER_NAME
        ]

        parameters.addAll(baseFile.toString(), leftFile.toString(), rightFile.toString())

        Path filesQuadruplePath = baseFile.getParent()
        Path outputPath = getOutputPath(filesQuadruplePath, DEFAULT_MERGE_FILE_NAME)
        parameters.addAll('-o', outputPath.toString())

        return parameters
    }
}
