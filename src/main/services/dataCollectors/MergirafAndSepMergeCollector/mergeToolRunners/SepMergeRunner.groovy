package services.dataCollectors.MergirafAndSepMergeCollector.mergeToolRunners

import services.util.MergeToolRunner
import util.ProcessRunner

import java.nio.file.Path
import java.nio.file.Paths

class SepMergeRunner extends MergeToolRunner {
    private static final Path SEPMERGE_PATH = Paths.get('dependencies/sepmerge.jar')

    private boolean extensive

    SepMergeRunner(boolean extensive) {
        this.extensive = extensive
        this.mergeToolName = "sepmerge-${!extensive ? 'default' : 'extensive'}".toString()
    }

    protected ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile) {
        String processDirectory = SEPMERGE_PATH.getParent().toString()
        return ProcessRunner.buildProcess(processDirectory)
    }

    protected List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile) {
        String jarFileName = SEPMERGE_PATH.getFileName().toString()
        List<String> parameters = ['java', '-jar', jarFileName]
        parameters.addAll(leftFile.toString(), baseFile.toString(), rightFile.toString())

        Path filesQuadruplePath = baseFile.getParent()
        Path outputPath = getOutputPath(filesQuadruplePath, DEFAULT_MERGE_FILE_NAME)
        parameters.addAll("-o=${outputPath.toString()}".toString())

        if (this.extensive) {
            parameters.add('-e')
        }

        return parameters
    }
}
