package services.dataCollectors.S3MWithCSDiffCollector

import services.util.MergeConflict
import services.util.Utils
import util.TextualMergeStrategy

import java.nio.file.Path
import org.apache.commons.lang3.StringUtils

class MergeSummary {

    private static final String MERGE_FILE_NAME = "merge.java"
    private static final String TEXTUAL_MERGE_FILE_NAME = "textual.java"

    // Path with the base, left, right and merge files involved in the merge
    Path filesQuadruplePath

    Map<String, Integer> numberOfConflictsPerApproach
    Map<String, Map<String, Boolean>> approachesHaveSameOutputs
    Map<String, Map<String, Boolean>> approachesHaveSameConflicts

    MergeSummary(Path filesQuadruplePath) {
        this.filesQuadruplePath = Utils.getOutputPath().resolve(filesQuadruplePath)
        compareMergeApproaches()
    }

    private void compareMergeApproaches() {
        Map<String, Path> mergeOutputPaths = getMergeOutputPaths()
        Map<String, String> mergeOutputs = getMergeOutputs(mergeOutputPaths)
        Map<String, Set<MergeConflict>> mergeConflicts = getMergeConflicts(mergeOutputPaths)

        this.numberOfConflictsPerApproach = [:]
        mergeConflicts.each { approach, conflicts ->
            this.numberOfConflictsPerApproach.put(approach, conflicts.size())
        }

        this.approachesHaveSameOutputs = [:]
        this.approachesHaveSameConflicts = [:]

        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            this.approachesHaveSameOutputs.put(approach1, [:])
            this.approachesHaveSameConflicts.put(approach1, [:])

            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]

                // Merge outputs are compared disregarding whitespaces
                String output1 = StringUtils.deleteWhitespace(mergeOutputs.get(approach1))
                String output2 = StringUtils.deleteWhitespace(mergeOutputs.get(approach2))
                this.approachesHaveSameOutputs.get(approach1).put(approach2, output1 == output2)

                Set<MergeConflict> conflicts1 = mergeConflicts.get(approach1)
                Set<MergeConflict> conflicts2 = mergeConflicts.get(approach2)
                this.approachesHaveSameConflicts.get(approach1).put(approach2, conflicts1 == conflicts2)
            }
        }
    }

    private Map<String, Path> getMergeOutputPaths() {
        Map<String, Path> mergeOutputPaths = [:]
        mergeOutputPaths.put("Textual", getTextualMergeOutputPath())
        mergeOutputPaths.put("Actual", getActualMergeOutputPath())

        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            mergeOutputPaths.put(strategy.name(), getMergeStrategyOutputPath(strategy))
        }

        return mergeOutputPaths
    }

    private Path getTextualMergeOutputPath() {
        return this.filesQuadruplePath.resolve(TEXTUAL_MERGE_FILE_NAME)
    }

    private Path getActualMergeOutputPath() {
        return this.filesQuadruplePath.resolve(MERGE_FILE_NAME)
    }

    private Path getMergeStrategyOutputPath(TextualMergeStrategy strategy) {
        return this.filesQuadruplePath.resolve(strategy.name()).resolve(MERGE_FILE_NAME)
    }

    private Map<String, String> getMergeOutputs(Map<String, Path> mergeOutputPaths) {
        Map<String, String> outputs = [:]
        mergeOutputPaths.each { approach, mergeOutputPath ->
            String output = getMergeOutput(mergeOutputPath)
            outputs.put(approach, output)
        }

        return outputs
    }

    private String getMergeOutput(Path mergeOutputPath) {
        return mergeOutputPath.getText()
    }

    private Map<String, Set<MergeConflict>> getMergeConflicts(Map<String, Path> mergeOutputPaths) {
        Map<String, Set<MergeConflict>> conflicts = [:]
        mergeOutputPaths.each { approach, mergeOutputPath ->
            Set<MergeConflict> currentConflicts = getMergeConflicts(mergeOutputPath)
            conflicts.put(approach, currentConflicts)
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
            values.add(Integer.toString(this.numberOfConflictsPerApproach.get(approach)))
        }

        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]

                boolean sameOutput = this.approachesHaveSameOutputs.get(approach1).get(approach2)
                values.add(Boolean.toString(sameOutput))
            }
        }

        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]

                boolean sameConflict = this.approachesHaveSameConflicts.get(approach1).get(approach2)
                values.add(Boolean.toString(sameConflict))
            }
        }

        return values.join(',')
    }

}