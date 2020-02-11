package services

import main.util.FileManager
import main.interfaces.DataCollector

import main.project.MergeCommit
import main.project.Project

import static main.app.MiningFramework.arguments

class RevisionsFilesCollector implements DataCollector {

    public void collectData(Project project, MergeCommit mergeCommit) {
        Set<String> modifiedFiles = getMutuallyModifiedFiles(project, mergeCommit);


        for (String filePath: modifiedFiles) {
            collectDataFromFile(project, mergeCommit, filePath);
        }

        println "${project.getName()} - Revisions collection finished!"
    }

    // This method is used within the modified lines data collector save revisions from a specific file
    public String collectDataFromFile(Project project, MergeCommit mergeCommit, String filePath) {
        String classFilePath = getClassFilePath(project, mergeCommit, filePath);
        
        String revisionsFolderPath = "${arguments.getOutputPath()}/files/${project.getName()}/${mergeCommit.getSHA()}/${classFilePath}/";
        File revisionsFolder = new File(revisionsFolderPath);
        if(!revisionsFolder.exists()) {
            revisionsFolder.mkdirs()
        }

        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getLeftSHA(), "${revisionsFolderPath}/left.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getRightSHA(), "${revisionsFolderPath}/right.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getAncestorSHA(), "${revisionsFolderPath}/base.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getSHA(), "${revisionsFolderPath}/merge.java")     
    }
    
    private String getClassFilePath(Project project, MergeCommit mergeCommit, String filePath) {
        String className = TypeNameHelper.getFullyQualifiedName(project, filePath, mergeCommit.getAncestorSHA())

        return className.replaceAll('\\.', '\\/')
    }  

    private Set<String> getMutuallyModifiedFiles(Project project, MergeCommit mergeCommit) {
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())

        return leftModifiedFiles.intersect(rightModifiedFiles)
    }

}