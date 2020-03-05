package services

@Grab(group='org.apache.commons', module='commons-lang3', version='3.9')
import main.interfaces.DataCollector
import main.project.MergeCommit
import main.project.CommitPair
import main.project.Project
import main.util.FileManager
import main.util.FileTransformations
import main.util.ProcessRunner
import main.util.MergeHelper
import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern
import static groovy.io.FileType.FILES

import static main.app.MiningFramework.arguments

class ExperimentalDataCollectorCommitPair extends ExperimentalDataCollectorImpl {

    void collectData(Project project, CommitPair commitPair) {
        setUp()

        collectMutuallyModifiedMethodsAndAttributes(project, commitPair)
        println "${project.getName()} - Data collection finished!"
    }

    @Override
    protected String addHeaderLinesForOutputFile(){
        return 'project;analysis_type;commit one;commit two;className;method;local_version_commit_one;local_version_commit_two\n'
    }
    
    protected String addCommitPairInfoIntoOutputFile(Project project, CommitPair commitPair, String className, String modifiedDeclarationSignature){
        return "${project.getName()};commit_pairs;${commitPair.getSHACommitOne()};${commitPair.getSHACommitTwo()};${className};\"${modifiedDeclarationSignature.replace(",","|")}\";${FileManager.findLocalClassFilesDirectory(commitPair.getLocalPathCommitOne())};${FileManager.findLocalClassFilesDirectory(commitPair.getLocalPathCommitTwo())}\n"
    }

    private void callForCodeTransformations(String directory, String className, String commitSHA) {
        println(className)
        for (file in FileManager.findLocalFileOfChangedClass(directory, className, commitSHA)){
            FileTransformations.executeCodeTransformations(file.toString().replace("[","").replace("]",""))
        }
    }

    private void collectMutuallyModifiedMethodsAndAttributes(Project project, CommitPair commitPair) {
        Set<String> modifiedFilePaths = collectModifiedFiles(project, commitPair)
        for(String filePath in modifiedFilePaths) {
            if (!filePath.toString().endsWith("Test.java")){
                Set<ModifiedDeclaration> allModifiedMethodsAndAttributes = super.getModifiedMethodsAndAttributes(project, filePath, commitPair.getSHACommitOne(), commitPair.getSHACommitTwo())
                String className
                if (allModifiedMethodsAndAttributes.size() == 0){
                    allModifiedMethodsAndAttributes = getModifiedMethodsAndAttributes(filePath, commitPair.getSHACommitOne(), commitPair.getLocalPathCommitOne(), commitPair.getSHACommitTwo(), commitPair.getLocalPathCommitTwo())
                    className = getClassFullyQualifiedName(filePath, commitPair.getSHACommitOne(), commitPair.getLocalPathCommitOne())
                }else{
                    className = super.getClassFullyQualifiedName(project, filePath, commitPair.getSHACommitOne())
                }

                callForCodeTransformations(commitPair.getLocalPathCommitOne(), (className.split("\\.") as Set).last(), commitPair.getSHACommitOne())
                callForCodeTransformations(commitPair.getLocalPathCommitTwo(), (className.split("\\.") as Set).last(), commitPair.getSHACommitTwo())
                
                /*if (commitPair.getLocalPathCommitOne() != ""){
                    allModifiedMethodsAndAttributes = getModifiedMethodsAndAttributes(filePath, commitPair.getSHACommitOne(), commitPair.getLocalPathCommitOne(), commitPair.getSHACommitTwo(), commitPair.getLocalPathCommitTwo())
                    className = getClassFullyQualifiedName(filePath, commitPair.getSHACommitOne(), commitPair.getLocalPathCommitOne())
                }else{
                    allModifiedMethodsAndAttributes = super.getModifiedMethodsAndAttributes(project, filePath, commitPair.getSHACommitOne(), commitPair.getSHACommitTwo())
                    className = super.getClassFullyQualifiedName(project, filePath, commitPair.getSHACommitOne())
                }*/

                for(declaration in allModifiedMethodsAndAttributes) {
                    storeModifiedAttributesAndMethods(project, commitPair, className, declaration, filePath)
                }

                saveCommitPairFiles(project, commitPair, className.replaceAll('\\.', '\\/'), filePath)
            }
        }
    }

    private void saveCommitPairFiles(Project project, CommitPair commitPair, String classFilePath, String filePath) {
        String outputPath = arguments.getOutputPath()
        
        String path = "${outputPath}/files/${project.getName()}/${commitPair.getSHACommitOne()}/${classFilePath}/"
        File results = new File(path)
        if(!results.exists())
            results.mkdirs()

        FileManager.copyAndMoveFile(project, filePath, commitPair.getSHACommitOne(), "${path}/commit-one.java")
        FileManager.copyAndMoveFile(project, filePath, commitPair.getSHACommitTwo(), "${path}/commit-two.java")
    }

    private Set<String> collectModifiedFiles(Project project, CommitPair commitPair) {
        Set<String> modifiedFiles = FileManager.getModifiedFiles(project, commitPair.getSHACommitOne(), commitPair.getSHACommitTwo())
        if (modifiedFiles.size() == 0){
            modifiedFiles = FileManager.getModifiedFilesLocalOption(project, commitPair.getLocalPathCommitOne(), commitPair.getLocalPathCommitTwo())
        }
        return modifiedFiles
    }

    private Set<ModifiedDeclaration> getModifiedMethodsAndAttributes(String filePath, String commitOneSHA, String commitOnePath, String commitTwoSHA, String commitTwoPath) {
        File ancestorFile = FileManager.getFileLocal(filePath, commitOneSHA, commitOnePath)
        File targetFile = FileManager.getFileLocal(filePath, commitTwoSHA, commitTwoPath)

        String[] diffResultLines = runDiffJ(ancestorFile, targetFile)
        return getModifiedMethodsAndAttributes(diffResultLines)
    }

    private void storeModifiedAttributesAndMethods(Project project, CommitPair commitPair, String className, ModifiedDeclaration declaration, String filePath) {
            printResults(project, commitPair, className, declaration.getSignature())
    }

    private synchronized void printResults(Project project, CommitPair commitPair, String className, String modifiedDeclarationSignature) {

        File resultsFile = new File("${arguments.getOutputPath()}/data/results.csv")

        resultsFile << addCommitPairInfoIntoOutputFile(project, commitPair, className, modifiedDeclarationSignature)

    }

    protected String addMergeCommitInfoIntoOutputFile(Project project, CommitPair commitPair, String className, String modifiedDeclarationSignature){
        return "${project.getName()};${commitPair.getSHACommitOne()};${commitPair.getSHACommitTwo()};${className};${modifiedDeclarationSignature}\n"
    }

    private String getClassFullyQualifiedName(String filePath, String commitSHA, String directoryPath) {
        String className = getClassName(filePath)
        String classPackage = getClassPackage(filePath, commitSHA, directoryPath)

        return (classPackage == "" ? "" : classPackage + '.') + className
    }

    static String getClassPackage(String filePath, String commitSHA, String directoryPath) {
        File gitCatFile = new File(directoryPath+"/"+filePath)

        for (String fileLine : gitCatFile) {
            String lineNoWhitespace = StringUtils.deleteWhitespace(fileLine)
            if(lineNoWhitespace.take(7) == 'package') {
                return lineNoWhitespace.substring(7, lineNoWhitespace.indexOf(';')) // assuming the ; will be at the same line
            }
        }

        return ""
    }

}