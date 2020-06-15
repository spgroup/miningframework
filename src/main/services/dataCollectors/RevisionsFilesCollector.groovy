package services.dataCollectors

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import util.TypeNameHelper
import util.FileManager

import static app.MiningFramework.arguments

/**
 * @provides: for each file modified by both parents, creates a folder with the format:
 * [outputPath]/files/[projectName]/[commitSha]/ with base, left, right and merge versions of that file
 */
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

        return revisionsFolder
    }

    private File createRevisionsFolderIfItDoesntExist (Project project, MergeCommit mergeCommit, String filePath) {
        String classFilePath = getClassFilePath(project, mergeCommit, filePath);
        
        return createDirectory(project, mergeCommit, classFilePath)
    }

    public File createBuildFolderIfItDoesntExist (Project project, MergeCommit mergeCommit, String directoryName) {
        createDirectory(project, mergeCommit, directoryName)
    }

    private File createDirectory(Project project, MergeCommit mergeCommit, String directoryName) {
        String revisionsFolderPath = "${arguments.getOutputPath()}/files/${project.getName()}/${mergeCommit.getSHA()}/${directoryName}/";

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