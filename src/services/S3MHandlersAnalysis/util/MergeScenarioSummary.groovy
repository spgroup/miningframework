package services.S3MHandlersAnalysis.util

import org.apache.commons.lang3.StringUtils
import services.S3MHandlersAnalysis.Handlers
import services.S3MHandlersAnalysis.implementations.OutputProcessor

import java.nio.file.Path

class MergeScenarioSummary {

    Path mergeScenario
    List<Integer> numberOfConflicts
    List<Boolean> differenceBetweenMergeResults
    List<Boolean> differenceBetweenConflictSets

    MergeScenarioSummary(Path mergeScenario) {
        Set<MergeConflict> TMConflicts = getMergeConflicts(mergeScenario, Handlers.mergeResultPaths[0])
        Set<MergeConflict> CTConflicts = getMergeConflicts(mergeScenario, Handlers.mergeResultPaths[1])
        Set<MergeConflict> SFConflicts = getMergeConflicts(mergeScenario, Handlers.mergeResultPaths[2])
        Set<MergeConflict> MMConflicts = getMergeConflicts(mergeScenario, Handlers.mergeResultPaths[3])
        Set<MergeConflict> KBConflicts = getMergeConflicts(mergeScenario, Handlers.mergeResultPaths[4])

        this.mergeScenario = Utils.getOutputPath().relativize(mergeScenario)

        this.numberOfConflicts = [
                TMConflicts.size(),
                CTConflicts.size(),
                SFConflicts.size(),
                MMConflicts.size(),
                KBConflicts.size()
        ]

        this.differenceBetweenConflictSets = [
                CTConflicts == SFConflicts,
                CTConflicts == SFConflicts,
                CTConflicts == SFConflicts,
                SFConflicts == MMConflicts,
                SFConflicts == KBConflicts,
                MMConflicts == KBConflicts
        ]

        String CTText = getFileText(mergeScenario, Handlers.mergeResultPaths[1])
        String SFText = getFileText(mergeScenario, Handlers.mergeResultPaths[2])
        String MMText = getFileText(mergeScenario, Handlers.mergeResultPaths[3])
        String KBText = getFileText(mergeScenario, Handlers.mergeResultPaths[4])

        this.differenceBetweenMergeResults = [
                equalsModuloWhitespace(CTText, SFText),
                equalsModuloWhitespace(CTText, MMText),
                equalsModuloWhitespace(CTText, KBText),
                equalsModuloWhitespace(SFText, MMText),
                equalsModuloWhitespace(SFText, KBText),
                equalsModuloWhitespace(MMText, KBText)
        ]

    }

    private static boolean equalsModuloWhitespace(String s1, String s2) {
        return StringUtils.deleteWhitespace(s1) == StringUtils.deleteWhitespace(s2)
    }

    private static Set<MergeConflict> getMergeConflicts(Path mergeScenario, String toResolve) {
        Path mergeFile = mergeScenario.resolve(toResolve)
        return MergeConflict.extractMergeConflicts(mergeFile)
    }

    private static String getFileText(Path mergeScenario, String toResolve) {
        Path mergeFile = mergeScenario.resolve(toResolve)
        return mergeFile.getText()
    }

    @Override
    String toString() {
        String mergeScenarioLink = Utils.getHyperLink(OutputProcessor.ANALYSIS_REMOTE_URL + '/' + mergeScenario.toString(), mergeScenario.getFileName().toString())

        return "${mergeScenarioLink},${Utils.toStringList(numberOfConflicts, ',')},${Utils.toStringList(differenceBetweenMergeResults, ',')},${Utils.toStringList(differenceBetweenConflictSets, ',')}"
    }


}
