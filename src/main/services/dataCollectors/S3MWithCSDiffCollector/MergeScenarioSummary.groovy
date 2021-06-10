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

        Map<String, Path> mergePaths = [:]
        mergePaths.put("Textual", getTextualMergeOutputPath())
        mergePaths.put("Actual", getActualMergeOutputPath())

        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            mergePaths.put(strategy.name(), getMergeOutputPath(strategy))
        }

        this.numberOfConflicts = [:]
        Map<String, String> outputs = [:]
        Map<String, Set<MergeConflict>> conflicts = [:]

        mergePaths.each { name, path ->
            Set<MergeConflict> currentConflicts = getMergeConflicts(path)
            this.numberOfConflicts.put(name, currentConflicts.size())
            conflicts.put(name, currentConflicts)
            outputs.put(name, getMergeOutput(path))
        }

        this.sameOutputs = [:]
        this.sameConflicts = [:]

        List<String> mergeApproaches = MergesCollector.getMergeApproaches()
        for (int i = 0; i < mergeApproaches.size(); i++) {
            String approach1 = mergeApproaches[i]
            this.sameOutputs.put(approach1, [:])
            this.sameConflicts.put(approach1, [:])

            for (int j = i + 1; j < mergeApproaches.size(); j++) {
                String approach2 = mergeApproaches[j]

                String output1 = StringUtils.deleteWhitespace(outputs.get(approach1))
                String output2 = StringUtils.deleteWhitespace(outputs.get(approach2))
                this.sameOutputs.get(approach1).put(approach2, output1 == output2)

                Set<MergeConflict> conflicts1 = conflicts.get(approach1)
                Set<MergeConflict> conflicts2 = conflicts.get(approach2)
                this.sameConflicts.get(approach1).put(approach2, conflicts1 == conflicts2)
            }
        }
    }

    boolean approachesHaveSameOutputs() {
        List<String> mergeApproaches = MergesCollector.getMergeApproaches()
        for (int i = 0; i < mergeApproaches.size(); i++) {
            for (int j = i + 1; j < mergeApproaches.size(); j++) {
                if (!this.sameOutputs.get(mergeApproaches[i]).get(mergeApproaches[j])) {
                    return false;
                }
            }
        }

        return true;
    }

    boolean approachesHaveSameConflicts() {
        List<String> mergeApproaches = MergesCollector.getMergeApproaches()
        for (int i = 0; i < mergeApproaches.size(); i++) {
            for (int j = i + 1; j < mergeApproaches.size(); j++) {
                if (!this.sameConflicts.get(mergeApproaches[i]).get(mergeApproaches[j])) {
                    return false;
                }
            }
        }

        return true;
    }

    private String getMergeOutput(Path mergePath) {
        return mergePath.getText()
    }

    private Set<MergeConflict> getMergeConflicts(Path mergePath) {
        return MergeConflict.extractMergeConflicts(mergePath)
    }

    private Path getTextualMergeOutputPath() {
        return this.mergeScenarioPath.resolve(TEXTUAL_FILE_NAME)
    }

    private Path getActualMergeOutputPath() {
        return this.mergeScenarioPath.resolve(MERGE_FILE_NAME)
    }

    private Path getMergeOutputPath(TextualMergeStrategy strategy) {
        return this.mergeScenarioPath.resolve(strategy.name()).resolve(MERGE_FILE_NAME)
    }

    @Override
    String toString() {
        List<String> values = [ this.mergeScenarioPath.getFileName() ]
        List<String> mergeApproaches = MergesCollector.getMergeApproaches()

        for (String approach: mergeApproaches) {
            values.add(Integer.toString(this.numberOfConflicts.get(approach)))
        }

        for (int i = 0; i < mergeApproaches.size(); i++) {
            String approach1 = mergeApproaches[i]
            for (int j = i + 1; j < mergeApproaches.size(); j++) {
                String approach2 = mergeApproaches[j]

                boolean sameOutput = this.sameOutputs.get(approach1).get(approach2)
                values.add(Boolean.toString(sameOutput))
            }
        }

        for (int i = 0; i < mergeApproaches.size(); i++) {
            String approach1 = mergeApproaches[i]
            for (int j = i + 1; j < mergeApproaches.size(); j++) {
                String approach2 = mergeApproaches[j]

                boolean sameConflict = this.sameConflicts.get(approach1).get(approach2)
                values.add(Boolean.toString(sameConflict))
            }
        }

        return values.join(',')
    }

}