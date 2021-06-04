package services.dataCollectors.S3MWithCSDiffCollector

import services.util.MergeConflict
import services.util.Utils
import util.TextualMergeStrategy

import java.nio.file.Path
import org.apache.commons.lang3.StringUtils

class MergeScenarioSummary {

    private static final String MERGE_FILE_NAME = "merge.java"

    Path mergeScenarioPath
    Map<TextualMergeStrategy, Integer> numberOfConflicts
    Map<TextualMergeStrategy, Map<TextualMergeStrategy, Boolean>> sameOutputs
    Map<TextualMergeStrategy, Map<TextualMergeStrategy, Boolean>> sameConflicts

    MergeScenarioSummary(Path mergeScenarioPath) {
        this.mergeScenarioPath = Utils.getOutputPath().resolve(mergeScenarioPath)

        this.numberOfConflicts = [:]
        Map<TextualMergeStrategy, String> outputs = [:]
        Map<TextualMergeStrategy, Set<MergeConflict>> conflicts = [:]

        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            outputs.put(strategy, getMergeOutput(strategy))
            Set<MergeConflict> currentConflicts = getMergeConflicts(strategy)

            conflicts.put(strategy, currentConflicts)
            this.numberOfConflicts.put(strategy, currentConflicts.size())
        }

        this.sameOutputs = [:]
        this.sameConflicts = [:]

        for (TextualMergeStrategy strategy1: MergesCollector.strategies) {
            this.sameOutputs.put(strategy1, [:])
            this.sameConflicts.put(strategy1, [:])

            for (TextualMergeStrategy strategy2: MergesCollector.strategies) {
                if (strategy1 != strategy2) {
                    String output1 = StringUtils.deleteWhitespace(outputs.get(strategy1))
                    String output2 = StringUtils.deleteWhitespace(outputs.get(strategy2))
                    this.sameOutputs.get(strategy1).put(strategy2, output1 == output2)

                    Set<MergeConflict> conflicts1 = conflicts.get(strategy1)
                    Set<MergeConflict> conflicts2 = conflicts.get(strategy2)
                    this.sameConflicts.get(strategy1).put(strategy2, conflicts1 == conflicts2)
                }
            }
        }
    }

    boolean strategiesHaveSameOutputs() {
        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            if (this.sameOutputs.values().contains(false)) {
                return false;
            }
        }

        return true;
    }

    boolean strategiesHaveSameConflicts() {
        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            if (this.sameConflicts.values().contains(false)) {
                return false;
            }
        }

        return true;
    }

    private String getMergeOutput(TextualMergeStrategy strategy) {
        return getMergeOutputPath(strategy).getText()
    }

    private Set<MergeConflict> getMergeConflicts(TextualMergeStrategy strategy) {
        return MergeConflict.extractMergeConflicts(getMergeOutputPath(strategy))
    }

    private Path getMergeOutputPath(TextualMergeStrategy strategy) {
        return this.mergeScenarioPath.resolve(strategy.name()).resolve(MERGE_FILE_NAME)
    }

    @Override
    String toString() {
        // TODO: Fill in here
    }

}