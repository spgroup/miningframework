package services.dataCollectors.csDiffCollector

import com.google.common.io.Files
import org.apache.commons.io.FileUtils;
import util.ProcessRunner;

import java.nio.file.Path;
import java.nio.file.Paths
import java.nio.file.StandardCopyOption;

public class CSDiffRunner {
    static final Path CS_DIFF_PATH = Paths.get("dependencies/csdiff.sh")

    /**
     * @param mergeScenarios
     */
    static void collectCSDiffResults(List<Path> mergeScenarios) {
        mergeScenarios.parallelStream()
                .forEach(mergeScenario -> runCSDiffForMergeScenario(mergeScenario))
    }

    private static void runCSDiffForMergeScenario(Path mergeScenario) {
        Path leftFile = getInvolvedFile(mergeScenario, 'left')
        Path baseFile = getInvolvedFile(mergeScenario, 'base')
        Path rightFile = getInvolvedFile(mergeScenario, 'right')

        runCSDiffProcess(leftFile, baseFile, rightFile, 'csdiff.java')
    }

    private static void runCSDiffProcess(Path leftFile, Path baseFile, Path rightFile, String outputFileName) {
        String outputPathFileName = getOutputPath(baseFile.getParent(), outputFileName).toString()
        String diff3PathFileName = getOutputPath(baseFile.getParent(), 'diff3.java').toString()

        Process csDiff = ProcessRunner.startProcess(buildCSDiffProcess(leftFile, baseFile, rightFile, outputPathFileName, diff3PathFileName))

        csDiff.waitFor()
    }

    private static ProcessBuilder buildCSDiffProcess(Path leftFile, Path baseFile, Path rightFile, String outputFileName, String diff3PathFileName) {
        ProcessBuilder csDiffProcessBuilder = ProcessRunner.buildProcess(getParentAsString(CS_DIFF_PATH))
        List<String> parameters = buildCSDiffParameters(leftFile, baseFile, rightFile, outputFileName, diff3PathFileName)
        csDiffProcessBuilder.command().addAll(parameters)
        return csDiffProcessBuilder
    }

    private static List<String> buildCSDiffParameters(Path leftFile, Path baseFile, Path rightFile, String outputFileName, String diff3PathFileName) {
        List<String> parameters = ['sh', getNameAsString(CS_DIFF_PATH), leftFile.toString(), baseFile.toString(), rightFile.toString(), outputFileName, diff3PathFileName]
        return parameters
    }

    private static String getParentAsString(Path path) {
        return path.getParent().toString()
    }

    private static String getNameAsString(Path path) {
        return path.getFileName().toString()
    }

    private static Path getOutputPath(Path baseFileParent, String fileName) {
        return baseFileParent.resolve(fileName)
    }

    private static Path getInvolvedFile(Path mergeScenario, String fileName) {
        return mergeScenario.resolve("${fileName}.java").toAbsolutePath()
    }
}
