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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils

import static app.MiningFramework.arguments

class MergeSummary {

    private static final String MERGE_FILE_NAME = "merge.java"
    static public Arguments arguments
    // Path with the base, left, right and merge files involved in the merge
    public Path filesQuadruplePath

    Map<String, Integer> numberOfConflictsPerApproach
    Map<String, Map<String, Boolean>> approachesHaveSameOutputs
    Map<String, Map<String, Boolean>> approachesHaveSameConflicts

    MergeSummary(Path filesQuadruplePath) {
        this.filesQuadruplePath = filesQuadruplePath
        //if(forwentMergeScenariosWithBehaviorDifferent()) {
          //  if(verifyExistFile(this.filesQuadruplePath)) {
                compareMergeApproaches()
            //}
        //}
    }
    private  boolean forwentMergeScenariosWithBehaviorDifferent() {
        Path newHandlerOutput = getContributionFile(this.filesQuadruplePath, MergesCollector.mergeApproaches[0])
        Path oldHandlerOutput = getContributionFile(this.filesQuadruplePath, MergesCollector.mergeApproaches[1])

        List<String> resultDiff =  runTextualDiff(newHandlerOutput, oldHandlerOutput)
        if(resultDiff !=null && resultDiff.size() > 0){
            //obtainResultsForProject(newHandlersFile, oldHandlersFile)
            return true;
        }else{
          //  obtainResultsForDiscartFile(getContributionFile(this.filesQuadruplePath), oldHandlersFile)
            //FileManager.delete(new File(leftFile.toString()))
            //FileManager.delete(new File(baseFile.toString()))
            // FileManager.delete(new File(rightFile.toString()))
            FileManager.delete(new File(this.filesQuadruplePath.toString()))
            String str = getMergeCommit(this.filesQuadruplePath.toString())
            println "${str}"
            FileManager.delete(new File(str))
            this.filesQuadruplePath = null;
            return false;
        }
    }
    private Path getContributionFile(Path filesQuadruplePath, String contributionFileName) {
        return filesQuadruplePath.resolve("${contributionFileName}.java").toAbsolutePath()
    }
    private static List<String> runTextualDiff(Path newHandlersFile, Path oldHandlersFile) {
        Process textDiff = ProcessRunner.runProcess(".", "diff" ,newHandlersFile.toString(), oldHandlersFile.toString())
        BufferedReader reader = new BufferedReader(new InputStreamReader(textDiff.getInputStream()))
        def output = reader.readLines()
        reader.close()
        return output
    }
    private String getMergeCommit(String path){
        path = path.replace("\\","/")
        String str = path.split("/")[0] + "\\"+path.split("/")[1] + "\\"+ path.split("/")[2]
        return str
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

               /* if(this.approachesHaveSameOutputs[approach1][approach2] == false){
                    FileManager.delete(new File(this.filesQuadruplePath.toString()))
                }
               */
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
       // mergeOutputPaths["GitMergeFile"] = getGitMergeFileOutputPath()
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

    private Path getGitMergeFileOutputPath() {
        return this.filesQuadruplePath.resolve("GitMergeFile").resolve(MERGE_FILE_NAME)
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
    private boolean verifyExistFile(Path mergeOutPath){
        File file = new File(mergeOutPath.toString())
        return file.getAbsoluteFile().exists();
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