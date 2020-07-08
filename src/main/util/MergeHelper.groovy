package util

import project.*
import java.util.regex.Pattern
import java.util.regex.Matcher
import org.apache.commons.io.FileUtils;

class MergeHelper {
    private static String CONFLICT_INDICATOR = "(CONFLICT)|(CONFLITO)"

    static public boolean hasMergeConflict (Project project, MergeCommit mergeCommit) {
        Process mergeSimulation = replayMergeScenario(project, mergeCommit)
        String mergeMessage = mergeSimulation.getText()
        
        Pattern pattern = Pattern.compile(CONFLICT_INDICATOR)
        Matcher m = pattern.matcher(mergeMessage)
       
        boolean result = m.find()

        Process returnToMaster = returnToMaster(project)
        returnToMaster.waitFor()
    
        return result
    }

    static public Process replayMergeScenario(Project project, MergeCommit mergeCommit) {
        Process checkoutLeft = ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', mergeCommit.getLeftSHA())
        checkoutLeft.waitFor()

        return ProcessRunner.runProcess(project.getPath(), 'git', 'merge', mergeCommit.getRightSHA())   
    }

    static public Process returnToMaster(Project project) {
        Process resetChanges = ProcessRunner.runProcess(project.getPath(), 'git', 'reset', '--hard')
        resetChanges.waitFor()

        return ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', 'master')
    }

    static public boolean areParentContributionsPreserved (Project project, MergeCommit mergeCommit, String fileName, String localRevisionFiles) {
        Process mergeSimulation = replayMergeScenario(project, mergeCommit)
        mergeSimulation.waitFor()
    
        boolean isThereDifference = isThereDiffBetweenFiles(project, fileName, localRevisionFiles)
        Process returnToMaster = returnToMaster(project)
        returnToMaster.waitFor()

        return isThereDifference
    }

    static private boolean isThereDiffBetweenFiles(Project project, String fileName, String localRevisionFiles) {
        File fileOriginalRepo = new File(findFileByName(fileName, project.getPath()))
        File fileReplayMerge = new File(findFileByName("merge", localRevisionFiles))
        
        return FileUtils.contentEquals(fileOriginalRepo, fileReplayMerge)
    }

    
    static private String findFileByName(String fileName, String pathToSearchOn) {
        def file = new FileNameByRegexFinder().getFileNames(pathToSearchOn, /${fileName}\.java/)[0]
        return file.toString()
    }

}