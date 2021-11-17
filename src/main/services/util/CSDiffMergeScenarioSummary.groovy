package services.util

import org.apache.commons.lang3.StringUtils
import services.outputProcessors.S3MOutputProcessor

import java.nio.file.Path

class CSDiffMergeScenarioSummary {

    Path mergeScenario
    List<Integer> numberOfConflicts
    Boolean hasDifferenceBetweenMergeResults
    Boolean hasDifferenceFromCSDiffToActualMerge
    Boolean hasDifferenceFromTextualToActualMerge
    Boolean hasDifferenceFromGitMergeToActualMerge

    CSDiffMergeScenarioSummary(Path mergeScenario) {
        Integer gitMergeConflictsNumber = getMergeConflictsNumber(mergeScenario, "git_merge")
        Integer mergeConflictsNumber = getMergeConflictsNumber(mergeScenario, "diff3")
        Integer cSDiffConflictsNumber = getMergeConflictsNumber(mergeScenario, "csdiff")

        this.mergeScenario = Utils.getOutputPath().relativize(mergeScenario)

        this.numberOfConflicts = [
                mergeConflictsNumber,
                cSDiffConflictsNumber,
                gitMergeConflictsNumber
        ]

        String gitMergeText = getFileText(mergeScenario, "git_merge")
        String mergeText = getFileText(mergeScenario, "diff3")
        String csDiffText = getFileText(mergeScenario, "csdiff")
        String actualMergeText = getFileText(mergeScenario, "merge")

        this.hasDifferenceBetweenMergeResults = hasDifferenceModuloWhitespace(mergeText, csDiffText)

        if (cSDiffConflictsNumber == 0) {
            this.hasDifferenceFromCSDiffToActualMerge = hasDifferenceModuloWhitespace(actualMergeText, csDiffText)
        } else {
            this.hasDifferenceFromCSDiffToActualMerge = true
        }

        if (mergeConflictsNumber == 0) {
            this.hasDifferenceFromTextualToActualMerge = hasDifferenceModuloWhitespace(actualMergeText, mergeText)
        } else {
            this.hasDifferenceFromTextualToActualMerge = true
        }

        if (gitMergeConflictsNumber == 0) {
            this.hasDifferenceFromGitMergeToActualMerge = hasDifferenceModuloWhitespace(actualMergeText, gitMergeText)
        } else {
            this.hasDifferenceFromGitMergeToActualMerge = true
        }

    }

    private static boolean hasDifferenceModuloWhitespace(String s1, String s2) {
        return StringUtils.deleteWhitespace(s1) != StringUtils.deleteWhitespace(s2)
    }

    private static Integer getMergeConflictsNumber(Path mergeScenario, String toResolve) {
        Path mergeFile = mergeScenario.resolve(Utils.getfileNameWithExtension(toResolve))
        return MergeConflict.getConflictsNumber(mergeFile)
    }

    private static String getFileText(Path mergeScenario, String toResolve) {
        Path mergeFile = mergeScenario.resolve(Utils.getfileNameWithExtension(toResolve))
        return mergeFile.getText()
    }

    @Override
    String toString() {
        String mergeScenarioLink = Utils.getHyperLink(S3MOutputProcessor.ANALYSIS_REMOTE_URL + '/' + mergeScenario.toString(), mergeScenario.getFileName().toString())

        return "${mergeScenarioLink},${Utils.toStringList(numberOfConflicts, ',')},${not(hasDifferenceBetweenMergeResults).toString()},${not(hasDifferenceFromTextualToActualMerge).toString()},${not(hasDifferenceFromCSDiffToActualMerge).toString()},${not(hasDifferenceFromGitMergeToActualMerge).toString()}";
    }

    private static Boolean not(Boolean v) {
        return Boolean.logicalXor(v, Boolean.TRUE)
    }
}
