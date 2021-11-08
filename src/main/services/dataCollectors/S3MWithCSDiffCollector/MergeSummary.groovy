package services.dataCollectors.S3MWithCSDiffCollector

import services.util.MergeConflict
import services.util.Utils
import util.TextualMergeStrategy

import java.nio.file.Path
import org.apache.commons.lang3.StringUtils

class MergeSummary {

    private static final String MERGE_FILE_NAME = "merge.java"

    // Path with the base, left, right and merge files involved in the merge
    Path filesQuadruplePath

    Map<String, Integer> numberOfConflictsPerApproach
    Map<String, Map<String, Boolean>> approachesHaveSameOutputs
    Map<String, Map<String, Boolean>> approachesHaveSameConflicts

    MergeSummary(Path filesQuadruplePath) {
        this.filesQuadruplePath = filesQuadruplePath
        compareMergeApproaches()
    }

    private void compareMergeApproaches() {
        Map<String, Path> mergeOutputPaths = getMergeOutputPaths()
        Map<String, String> mergeOutputs = getMergeOutputs(mergeOutputPaths)
        Map<String, Set<MergeConflict>> mergeConflicts = getMergeConflicts(mergeOutputPaths)

        this.numberOfConflictsPerApproach = [:]
        mergeConflicts.each { approach, conflicts ->
            this.numberOfConflictsPerApproach[approach] = conflicts.size()
        }

        this.approachesHaveSameOutputs = [:]
        this.approachesHaveSameConflicts = [:]

        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            this.approachesHaveSameOutputs[approach1] = [:]
            this.approachesHaveSameConflicts[approach1] = [:]

            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]

                // Merge outputs are compared disregarding whitespaces
                String output1 = StringUtils.deleteWhitespace(mergeOutputs[approach1])
                String output2 = StringUtils.deleteWhitespace(mergeOutputs[approach2])
                this.approachesHaveSameOutputs[approach1][approach2] = output1 == output2

                Set<MergeConflict> conflicts1 = mergeConflicts[approach1]
                Set<MergeConflict> conflicts2 = mergeConflicts[approach2]
                this.approachesHaveSameConflicts[approach1][approach2] = conflicts1 == conflicts2
            }
        }
    }

    private Map<String, Path> getMergeOutputPaths() {
        Map<String, Path> mergeOutputPaths = [:]
        mergeOutputPaths["CSDiff"] = getCSDiffMergeOutputPath()
        mergeOutputPaths["Diff3"] = getDiff3MergeOutputPath()
        mergeOutputPaths["GitMergeFile"] = getGitMergeFileOutputPath()
        mergeOutputPaths["Actual"] = getActualMergeOutputPath()

        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            String key = "S3M${strategy.name()}"
            mergeOutputPaths[key] = getMergeStrategyOutputPath(strategy)
        }

        return mergeOutputPaths
    }

    private Path getCSDiffMergeOutputPath() {
        return this.filesQuadruplePath.resolve("CSDiff").resolve(MERGE_FILE_NAME)
    }

    private Path getDiff3MergeOutputPath() {
        return this.filesQuadruplePath.resolve("Diff3").resolve(MERGE_FILE_NAME)
    }

    private Path getGitMergeFileOutputPath() {
        return this.filesQuadruplePath.resolve("GitMergeFile").resolve(MERGE_FILE_NAME)
    }

    private Path getActualMergeOutputPath() {
        return this.filesQuadruplePath.resolve(MERGE_FILE_NAME)
    }

    private Path getMergeStrategyOutputPath(TextualMergeStrategy strategy) {
        String mergeFileName = getMergeStrategyOutputFileName(strategy)
        return this.filesQuadruplePath.resolve("S3M").resolve(mergeFileName)
    }

    private String getMergeStrategyOutputFileName(TextualMergeStrategy strategy) {
        return "${strategy.name()}.java"
    }

    private Map<String, String> getMergeOutputs(Map<String, Path> mergeOutputPaths) {
        Map<String, String> outputs = [:]
        MergesCollector.mergeApproaches.each { approach ->
            Path mergeOutputPath = mergeOutputPaths[approach]
            String output = getMergeOutput(mergeOutputPath)
            outputs[approach] = output
        }

        return outputs
    }

    private String getMergeOutput(Path mergeOutputPath) {
        return mergeOutputPath.getText()
    }

    private Map<String, Set<MergeConflict>> getMergeConflicts(Map<String, Path> mergeOutputPaths) {
        Map<String, Set<MergeConflict>> conflicts = [:]
        MergesCollector.mergeApproaches.each { approach ->
            Path mergeOutputPath = mergeOutputPaths[approach]
            Set<MergeConflict> currentConflicts = getMergeConflicts(mergeOutputPath)
            conflicts[approach] = currentConflicts
        }

        return conflicts
    }

    private Set<MergeConflict> getMergeConflicts(Path mergeOutputPath) {
        return MergeConflict.extractMergeConflicts(mergeOutputPath)
    }

    @Override
    String toString() {
        List<String> values = [ this.filesQuadruplePath.getFileName() ]
        for (String approach: MergesCollector.mergeApproaches) {
            values.add(Integer.toString(this.numberOfConflictsPerApproach[approach]))
        }

        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]

                boolean sameOutput = this.approachesHaveSameOutputs[approach1][approach2]
                values.add(Boolean.toString(sameOutput))
            }
        }

        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]

                boolean sameConflict = this.approachesHaveSameConflicts[approach1][approach2]
                values.add(Boolean.toString(sameConflict))
            }
        }

        return values.join(',')
    }

}