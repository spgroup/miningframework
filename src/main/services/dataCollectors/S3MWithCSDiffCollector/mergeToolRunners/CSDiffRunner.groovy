package services.dataCollectors.S3MWithCSDiffCollector.mergeToolRunners

import util.ProcessRunner
import services.util.MergeConflict
import services.util.MergeToolRunner

import java.nio.file.Path
import java.nio.file.Paths

class CSDiffRunner extends MergeToolRunner {

    private static final Path CSDIFF_PATH = Paths.get('dependencies/simple-csdiff.sh')
    private static final String CSDIFF_MERGE_FILE_NAME = 'merge'
    private static final String DIFF3_MERGE_FILE_NAME = 'diff3'

    CSDiffRunner() {
        this.mergeToolName = 'CSDiff'
    }

    protected ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile) {
        String processDirectory = CSDIFF_PATH.getParent().toString()
        return ProcessRunner.buildProcess(processDirectory)
    }

    protected List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile) {
        String scriptFileName = CSDIFF_PATH.getFileName().toString()
        List<String> parameters = ['sh', scriptFileName]
        parameters.addAll(leftFile.toString(), baseFile.toString(), rightFile.toString())

        Path filesQuadruplePath = baseFile.getParent()
        Path mainOutput = getOutputPath(filesQuadruplePath, CSDIFF_MERGE_FILE_NAME)
        Path otherOutput = getOutputPath(filesQuadruplePath, DIFF3_MERGE_FILE_NAME)
        parameters.addAll(mainOutput.toString(), otherOutput.toString())
        
        return parameters
    }

}