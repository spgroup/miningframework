package services
import main.interfaces.CommitFilter

import main.util.*
import main.project.*


class CommitFilterImpl implements CommitFilter {

    public boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return containsMutuallyModifiedMethods(project, mergeCommit)
    }

    private boolean containsMutuallyModifiedMethods(Project project, MergeCommit mergeCommit) {

        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())
        Set<String> mutuallyModifiedFiles = new HashSet<String>(leftModifiedFiles)
        mutuallyModifiedFiles.retainAll(rightModifiedFiles)

        for(file in mutuallyModifiedFiles) {
            Set<String> leftModifiedMethods = getModifiedMethods(project, file, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
            Set<String> rightModifiedMethods = getModifiedMethods(project, file, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())
            leftModifiedMethods.retainAll(rightModifiedMethods) // Intersection.

            if(leftModifiedMethods.size() > 0)
                return true
        }

        return false
    }

    private Set<String> getModifiedMethods(Project project, String filePath, String childSHA, String ancestorSHA) {
        Set<String> modifiedMethods = new HashSet<ModifiedMethod>()

        File childFile = FileManager.copyFile(project, filePath, childSHA) 
        File ancestorFile = FileManager.copyFile(project, filePath, ancestorSHA)

        Process diffJ = ProcessRunner.runProcess('dependencies', 'java', '-jar', 'diffj.jar', '--brief', ancestorFile.getAbsolutePath(), childFile.getAbsolutePath())
        diffJ.getInputStream().eachLine {
            int inIndex = it.indexOf("in ")
            if(inIndex != -1) {
                String signature = it.substring(inIndex + 3)
                modifiedMethods.add(signature)
            }
        }
        
        FileManager.delete(childFile)
        FileManager.delete(ancestorFile)

        return modifiedMethods
    }

}