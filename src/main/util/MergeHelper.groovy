package util

import project.*
import java.util.regex.Pattern
import java.util.regex.Matcher

class MergeHelper {
    private static String CONFLICT_INDICATOR = "(CONFLICT)|(CONFLITO)"
    private static String options = "--ignore-cr-at-eol --ignore-all-space --ignore-blank-lines --ignore-space-change"

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

    public static ArrayList<Boolean> checkForEmptyDiffByParents(Project project, MergeCommit mergeCommit, String className) {
        ArrayList<Boolean> diffStatesByParents = new ArrayList<Boolean>();
        
        diffStatesByParents.add(performDiffAnalysisByParents(project, options, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), className))
        diffStatesByParents.add(performDiffAnalysisByParents(project, options, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA(), className))
        diffStatesByParents.add(checkForEmptyDiff(project, mergeCommit))
        
        return diffStatesByParents
    }

    private static boolean checkForEmptyDiff(Project project, MergeCommit mergeCommit) {
        Process diffAnalysis = performDiffAnalysisDeffault(project, mergeCommit, options)
        boolean result = diffAnalysis.getText() == ""
        
        Process returnToMaster = returnToMaster(project)
        returnToMaster.waitFor()
    
        return result
    }

    private static Process performDiffAnalysisDeffault(Project project, MergeCommit mergeCommit, String optionsDiff) {
        Process checkoutLeft = ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', mergeCommit.getLeftSHA())
        checkoutLeft.waitFor()

        return ProcessRunner.runProcess(project.getPath(), 'git', 'diff', mergeCommit.getRightSHA(), optionsDiff)   
    }

    private static Boolean performDiffAnalysisByParents(Project project, String optionsDiff, String parentOne, String parentTwo, className) {
        String localPathClass = localPathClassForDiff(getListFilesChanged(project, parentOne, parentTwo), className)
        
        Process checkoutParentOne = ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', parentOne)
        checkoutParentOne.waitFor()

        return ProcessRunner.runProcess(project.getPath(), 'git', 'diff', parentTwo, optionsDiff+" "+localPathClass).getText() == ""   
    }

    private static String[] getListFilesChanged (Project project, String parentOne, String parentTwo) {
        //Process diffAnalysis 
        Process checkoutParentOne = ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', parentOne)
        checkoutParentOne.waitFor()

        Process diffAnalysis = ProcessRunner.runProcess(project.getPath(), 'git', 'diff', parentTwo, "--name-only")
        String[] filesList = diffAnalysis.getText().split("\n")
     
        Process returnToMaster = returnToMaster(project)
        returnToMaster.waitFor()

        return filesList
    }

    private static String localPathClassForDiff(String[] filesList, String className) {
        String newClassName = className.replaceAll("\\.","/")
        filesList.each{value ->
            if (value.contains(newClassName)){
                return value
            }
        }
        return ""
    }

    private static boolean getLocalPathForChangedClass(HashMap<String, Boolean> localChangedFiles, String reportChangedClass) {
        localChangedFiles.each{value, key ->
            if (value.include(reportChangedClass.replaceAll(".","/"))){
                return true
            }
        }
        return false
    }

}