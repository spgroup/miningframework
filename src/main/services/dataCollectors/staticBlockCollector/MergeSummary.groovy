package services.dataCollectors.staticBlockCollector

import arguments.Arguments
import services.util.MergeConflict
import services.util.Utils
import util.FileManager
import util.ProcessRunner

import java.util.stream.Stream
import java.io.File
import java.nio.file.Path
import java.io.FileInputStream
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors

import org.apache.commons.lang3.StringUtils


class MergeSummary {

    private static final String MERGE_FILE_NAME = "merge.java"
    static public Arguments arguments
    // Path with the base, left, right and merge files involved in the merge
    public Path filesQuadruplePath

    Map<String, Integer> numberOfConflictsPerApproach
    Map<String, Integer> numberOfConflictsPerInitializationBlockAndApproach
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
        Map<String, Integer> mergeConflictsInitializationBlock = getMergeConflictsForStaticBlock(mergeOutputPaths)

        this.numberOfConflictsPerApproach = [:]
        mergeConflicts.each { approach, conflicts ->
            this.numberOfConflictsPerApproach[approach] = conflicts.size()
        }

        this.numberOfConflictsPerInitializationBlockAndApproach = [:]
        mergeConflictsInitializationBlock.each { approach, conflicts ->
            this.numberOfConflictsPerInitializationBlockAndApproach[approach] = conflicts
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
        mergeOutputPaths["SimpleInitializationBlockHandler"] = getNewHandlerMergeOutputPath()
        mergeOutputPaths["InsertionLevelInitializationBlockHandler"] = getOldHandlerMergeOutputPath()
         mergeOutputPaths["Actual"] = getActualMergeOutputPath()

        for (String strategy: MergesCollector.strategies) {
            String key = "${strategy}"
            mergeOutputPaths[key] = getMergeStrategyOutputPath(strategy)
        }

        return mergeOutputPaths
    }

    private Path getNewHandlerMergeOutputPath() {
        return this.filesQuadruplePath.resolve("InsertionLevelInitializationBlockHandler")
    }

    private Path getOldHandlerMergeOutputPath() {
        return this.filesQuadruplePath.resolve("SimpleInitializationBlockHandler")
    }

    private Path getActualMergeOutputPath() {
        return this.filesQuadruplePath.resolve(MERGE_FILE_NAME)
    }

    private Path getMergeStrategyOutputPath(String strategy) {
        String mergeFileName = getMergeStrategyOutputFileName(strategy)
        return this.filesQuadruplePath.resolve(mergeFileName)
    }

    private String getMergeStrategyOutputFileName(String strategy) {
        if(strategy.equals('Actual')){
            return MERGE_FILE_NAME
        }
        return "${strategy}.java"
    }

    private Map<String, String> getMergeOutputs(Map<String, Path> mergeOutputPaths) {
        Map<String, String> outputs = [:]
        for (String strategy: MergesCollector.strategies) {
            Path mergeOutputPath = mergeOutputPaths[strategy]
            String output = getMergeOutput(mergeOutputPath)
            outputs[strategy] = output
        }

        return outputs
    }

    private String getMergeOutput(Path mergeOutputPath) {
        return mergeOutputPath.getText()
    }

    private Map<String, Set<MergeConflict>> getMergeConflicts(Map<String, Path> mergeOutputPaths) {
        Map<String, Set<MergeConflict>> conflicts = [:]
        for (String strategy: MergesCollector.strategies) {
            Path mergeOutputPath = mergeOutputPaths[strategy]
            Set<MergeConflict> currentConflicts = getMergeConflicts(mergeOutputPath)
            conflicts[strategy] = currentConflicts
        }

        return conflicts
    }
    private Map<String, Integer> getMergeConflictsForStaticBlock(Map<String, Path> mergeOutputPaths) {
        Map<String, Integer> conflicts = [:]
        for (String strategy: MergesCollector.strategies) {
            Path mergeOutputPath = mergeOutputPaths[strategy]
            Integer currentConflicts = getMergeConflictsForStaticBlock(mergeOutputPath)
            conflicts[strategy] = currentConflicts
        }

        return conflicts
    }
    private int getMergeConflictsForStaticBlock(Path mergeOutputPath){
        return MergeConflictStaticBlock.extractMergeConflictsForStaticBlock(mergeOutputPath)
    }
    private Set<MergeConflict> getMergeConflicts(Path mergeOutputPath) {
        return MergeConflict.extractMergeConflicts(mergeOutputPath)
    }

    @Override
    String toString() {
        List<String> values = [ this.filesQuadruplePath.getFileName() ]
        for (String approach: MergesCollector.mergeApproaches) {
            if(approach !=null) {
                if (this.numberOfConflictsPerApproach != null || this.numberOfConflictsPerApproach[approach] != null) {
                    values.add(Integer.toString(this.numberOfConflictsPerApproach[approach]))
                } else {
                    values.add(Integer.valueOf(0).toString())
                }
            }
        }
        for (String approach: MergesCollector.mergeApproaches) {
                if (this.numberOfConflictsPerInitializationBlockAndApproach != null || this.numberOfConflictsPerInitializationBlockAndApproach[approach] != null) {
                    values.add(Integer.toString(this.numberOfConflictsPerInitializationBlockAndApproach[approach]))
                } else {
                    values.add(Integer.valueOf(0).toString())
                }
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