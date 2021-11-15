package services.dataCollectors.csDiffCollector

import com.google.common.io.Files
import org.apache.commons.io.FileUtils;
import util.ProcessRunner;
import services.util.Utils

import java.nio.file.Path;
import java.nio.file.Paths
import java.nio.file.StandardCopyOption;

public class CSDiffRunner {
    static final Path CS_DIFF_PATH = Paths.get("dependencies/csdiff_v2.sh")

    /**
     * @param mergeScenarios
     * @param languageSeparators
     */
    static void collectCSDiffResults(List<Path> mergeScenarios, String languageSeparators) {
        mergeScenarios.parallelStream()
                .forEach(mergeScenario -> runCSDiffForMergeScenario(languageSeparators, mergeScenario))
    }

    private static void runCSDiffForMergeScenario(String languageSeparators, Path mergeScenario) {
        Path leftFile = getInvolvedFile(mergeScenario, "left")
        Path baseFile = getInvolvedFile(mergeScenario, "base")
        Path rightFile = getInvolvedFile(mergeScenario, "right")

        runCSDiffProcess(languageSeparators, leftFile, baseFile, rightFile)
    }

    private static void runCSDiffProcess(String languageSeparators, Path leftFile, Path baseFile, Path rightFile) {
        Process csDiff = ProcessRunner.startProcess(buildCSDiffProcess(languageSeparators, leftFile, baseFile, rightFile))

        csDiff.waitFor()
    }

    private static ProcessBuilder buildCSDiffProcess(String languageSeparators, Path leftFile, Path baseFile, Path rightFile) {
        ProcessBuilder csDiffProcessBuilder = ProcessRunner.buildProcess(getParentAsString(CS_DIFF_PATH))
        List<String> parameters = buildCSDiffParameters(languageSeparators, leftFile, baseFile, rightFile)
        csDiffProcessBuilder.command().addAll(parameters)
        return csDiffProcessBuilder
    }

    private static List<String> buildCSDiffParameters(String languageSeparators, Path leftFile, Path baseFile, Path rightFile) {
        List<String> parameters = ['bash', getNameAsString(CS_DIFF_PATH), String.format("-s \"%s\"", languageSeparators), leftFile.toString(), baseFile.toString(), rightFile.toString()]

        return parameters
    }

    private static String getParentAsString(Path path) {
        return path.getParent().toString()
    }

    private static String getNameAsString(Path path) {
        return path.getFileName().toString()
    }

    private static Path getOutputPath(Path baseFileParent, String fileName) {
        return baseFileParent.resolve(Utils.getfileNameWithExtension(fileName))
    }

    private static Path getInvolvedFile(Path mergeScenario, String fileName) {
        return mergeScenario.resolve(Utils.getfileNameWithExtension(fileName)).toAbsolutePath()
    }
}
