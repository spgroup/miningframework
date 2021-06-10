package services.dataCollectors.S3MWithCSDiffCollector

import services.util.MergeConflict
import services.util.Utils
import util.TextualMergeStrategy

import java.nio.file.Path
import org.apache.commons.lang3.StringUtils

class MergeScenarioSummary {

    private static final String MERGE_FILE_NAME = "merge.java"
    private static final String TEXTUAL_FILE_NAME = "textual.java"

    Path mergeScenarioPath
    Map<String, Integer> numberOfConflicts
    Map<String, Map<String, Boolean>> sameOutputs
    Map<String, Map<String, Boolean>> sameConflicts

    MergeScenarioSummary(Path mergeScenarioPath) {
        this.mergeScenarioPath = Utils.getOutputPath().resolve(mergeScenarioPath)
        compareMergeApproaches()
    }

    boolean approachesHaveSameOutputs() {
        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]
                if (!this.sameOutputs.get(approach1).get(approach2)) {
                    return false;
                }
            }
        }

        return true;
    }

    boolean approachesHaveSameConflicts() {
        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]
                if (!this.sameConflicts.get(approach1).get(approach2)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void compareMergeApproaches() {
        Map<String, Path> mergePaths = getMergeOutputPaths()
        Map<String, String> mergeOutputs = getMergeOutputs(mergePaths)
        Map<String, Set<MergeConflict>> mergeConflicts = getMergeConflicts(mergePaths)

        this.numberOfConflicts = [:]
        mergeConflicts.each { approach, conflicts ->
            this.numberOfConflicts.put(approach, conflicts.size())
        }

        this.sameOutputs = [:]
        this.sameConflicts = [:]

        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            this.sameOutputs.put(approach1, [:])
            this.sameConflicts.put(approach1, [:])

            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]

                String output1 = StringUtils.deleteWhitespace(mergeOutputs.get(approach1))
                String output2 = StringUtils.deleteWhitespace(mergeOutputs.get(approach2))
                this.sameOutputs.get(approach1).put(approach2, output1 == output2)

                Set<MergeConflict> conflicts1 = mergeConflicts.get(approach1)
                Set<MergeConflict> conflicts2 = mergeConflicts.get(approach2)
                this.sameConflicts.get(approach1).put(approach2, conflicts1 == conflicts2)
            }
        }
    }

    private Map<String, Path> getMergeOutputPaths() {
        Map<String, Path> mergePaths = [:]
        mergePaths.put("Textual", getTextualMergeOutputPath())
        mergePaths.put("Actual", getActualMergeOutputPath())

        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            mergePaths.put(strategy.name(), getMergeStrategyOutputPath(strategy))
        }

        return mergePaths
    }

    private Path getTextualMergeOutputPath() {
        return this.mergeScenarioPath.resolve(TEXTUAL_FILE_NAME)
    }

    private Path getActualMergeOutputPath() {
        return this.mergeScenarioPath.resolve(MERGE_FILE_NAME)
    }

    private Path getMergeStrategyOutputPath(TextualMergeStrategy strategy) {
        return this.mergeScenarioPath.resolve(strategy.name()).resolve(MERGE_FILE_NAME)
    }

    private Map<String, String> getMergeOutputs(Map<String, Path> mergePaths) {
        Map<String, String> outputs = [:]
        mergePaths.each { approach, mergePath ->
            String output = getMergeOutput(mergePath)
            outputs.put(approach, output)
        }

        return outputs
    }

    private String getMergeOutput(Path mergePath) {
        return mergePath.getText()
    }

    private Map<String, Set<MergeConflict>> getMergeConflicts(Map<String, Path> mergePaths) {
        Map<String, Set<MergeConflict>> conflicts = [:]
        mergePaths.each { approach, mergePath ->
            Set<MergeConflict> currentConflicts = getMergeConflicts(mergePath)
            conflicts.put(approach, currentConflicts)
        }

        return conflicts
    }

    private Set<MergeConflict> getMergeConflicts(Path mergePath) {
        return MergeConflict.extractMergeConflicts(mergePath)
    }

    @Override
    String toString() {
        List<String> values = [ this.mergeScenarioPath.getFileName() ]
        for (String approach: MergesCollector.mergeApproaches) {
            values.add(Integer.toString(this.numberOfConflicts.get(approach)))
        }

        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]

                boolean sameOutput = this.sameOutputs.get(approach1).get(approach2)
                values.add(Boolean.toString(sameOutput))
            }
        }

        for (int i = 0; i < MergesCollector.mergeApproaches.size(); i++) {
            String approach1 = MergesCollector.mergeApproaches[i]
            for (int j = i + 1; j < MergesCollector.mergeApproaches.size(); j++) {
                String approach2 = MergesCollector.mergeApproaches[j]

                boolean sameConflict = this.sameConflicts.get(approach1).get(approach2)
                values.add(Boolean.toString(sameConflict))
            }
        }

        return values.join(',')
    }

}