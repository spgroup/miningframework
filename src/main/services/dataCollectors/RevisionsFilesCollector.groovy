package services.dataCollectors

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import util.TypeNameHelper
import util.FileManager

import static app.MiningFramework.arguments

class RevisionsFilesCollector implements DataCollector {

    public void collectData(Project project, MergeCommit mergeCommit) {
        Set<String> modifiedFiles = getFilesModifiedByBothParents(project, mergeCommit);


        for (String filePath: modifiedFiles) {
            collectDataFromFile(project, mergeCommit, filePath);
        }

        println "${project.getName()} - Revisions collection finished!"
    }

    public String collectDataFromFile(Project project, MergeCommit mergeCommit, String filePath) {
        File revisionsFolder = createRevisionsFolderIfItDoesntExist(project, mergeCommit, filePath);
        
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getLeftSHA(), "${revisionsFolder.getAbsolutePath()}/left.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getRightSHA(), "${revisionsFolder.getAbsolutePath()}/right.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getAncestorSHA(), "${revisionsFolder.getAbsolutePath()}/base.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getSHA(), "${revisionsFolder.getAbsolutePath()}/merge.java")     
    }

    private File createRevisionsFolderIfItDoesntExist (Project project, MergeCommit mergeCommit, String filePath) {
        String classFilePath = getClassFilePath(project, mergeCommit, filePath);
        
        String revisionsFolderPath = "${arguments.getOutputPath()}/files/${project.getName()}/${mergeCommit.getSHA()}/${classFilePath}/";
        File revisionsFolder = new File(revisionsFolderPath);
        if(!revisionsFolder.exists()) {
            revisionsFolder.mkdirs()
        }

        return revisionsFolder
    }
    
    private String getClassFilePath(Project project, MergeCommit mergeCommit, String filePath) {
        String className = TypeNameHelper.getFullyQualifiedName(project, filePath, mergeCommit.getAncestorSHA())

        return className.replaceAll('\\.', '\\/')
    }  

    private Set<String> getFilesModifiedByBothParents(Project project, MergeCommit mergeCommit) {
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())

        return leftModifiedFiles.intersect(rightModifiedFiles)
    }

}