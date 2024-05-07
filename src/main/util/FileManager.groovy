package util

import java.nio.file.Files 
import java.nio.file.Paths
import java.nio.file.Path
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import org.apache.commons.io.FileUtils 

import project.*

final class FileManager {

    public static Set<String> getModifiedFiles(Project project, String childSHA, String ancestorSHA) {
        Set<String> modifiedFiles = new HashSet<String>()
        
        Process gitDiff = ProcessRunner.runProcess(project.getPath(), 'git', 'diff', '--name-only', childSHA, ancestorSHA)
        gitDiff.getInputStream().eachLine {
            if(it.endsWith('.java'))
                modifiedFiles.add(it)
        }

        return modifiedFiles
    }

    public static Set<String> getModifiedFiles(Project project, String childSHA, String ancestorSHA, String fileExtension) {
        Set<String> modifiedFiles = new HashSet<String>()
        
        Process gitDiff = ProcessRunner.runProcess(project.getPath(), 'git', 'diff', '--name-only', childSHA, ancestorSHA)
        gitDiff.getInputStream().eachLine {
            if(it.endsWith(fileExtension))
                modifiedFiles.add(it)
        }

        return modifiedFiles
    }

    public static Set<String> getModifiedFilesLocalOption(Project project, String pathCommitOne, String pathCommitTwo) {
        def command = "diff -qr "+pathCommitOne+" "+pathCommitTwo
        def proc = command.execute()
        Set<String> modifiedFiles = new HashSet<String>()
        String[] lines = proc.in.text.toString().split("\n")
        for(int i=0; i < lines.size(); i++){
            if(lines[i].toString().contains('.java')){
                modifiedFiles.add(lines[i].toString().findAll(pathCommitOne+"[/a-zA-Z0-9.]*.java")[0].toString().split(pathCommitOne+"/")[-1])
            }
        }
        return modifiedFiles
    }
    
    public static File copyFile(Project project, String path, String SHA) {
        Process gitCatFile = ProcessRunner.runProcess(project.getPath(), 'git', 'cat-file', '-p', "${SHA}:${path}")    
        
        StringBuilder sb = new StringBuilder();
        File target = new File("${SHA}.java")

        gitCatFile.getInputStream().eachLine {
            sb.append(it)
            sb.append("\n")
        }

        target << sb.toString()

        return target
    }

    public static void copyAndMoveFile(Project project, String file, String sha, String target) {
        File targetFile = copyFile(project, file, sha)
        Files.move(targetFile.toPath(), Paths.get(target), REPLACE_EXISTING)
    }

    public static void copyDirectory(String source, String target) {
        FileUtils.copyDirectory(new File(source), new File(target))
    }

    public static File createOutputFiles(String outputPath, boolean createLinksFile) {
        File outputDir = new File(outputPath)
        if (outputDir.exists())
            outputDir.delete()

        outputDir.mkdirs()

        return outputDir
    }


    public static delete(File file) {
        if (!file.isDirectory())
            file.delete()
        else {
            if (file.list().length == 0) 
                file.delete()
            else {
                String[] files = file.list()
                for (temp in files) {
                    delete(new File(file, temp))
                }
                if (file.list().length == 0) 
                    file.delete()
            }
        }
    }

    static File getFileInCommit(Project project, String filePath, String commitSHA) {
        Process gitCatFile = ProcessRunner.runProcess(project.getPath(), 'git', 'cat-file', '-p', "${commitSHA}:${filePath}")

        StringBuilder sb = new StringBuilder()
        File file = File.createTempFile("${commitSHA}", ".java")
        file.deleteOnExit()

        gitCatFile.getInputStream().eachLine {
            sb.append(it)
            sb.append("\n")
        }

        file << sb.toString()

        return file
    }

    static File getFileLocal(String filePath, String commitSHA, String directoryPath) {
        File gitCatFile = new File(directoryPath+"/"+filePath)

        StringBuilder sb = new StringBuilder()
        File file = File.createTempFile("${commitSHA}", ".java")
        file.deleteOnExit()

        gitCatFile.eachLine {
            sb.append(it)
            sb.append("\n")
        }

        file << sb.toString()

        return file
    }

    public static String findLocalClassFilesDirectory(String directoryPath) {
        if (Files.exists(Paths.get(directoryPath+"/target/classes"))){
            return directoryPath+"/target/classes"
        }else if (Files.exists(Paths.get(directoryPath+"/build/classes"))){
            return directoryPath+"/build/classes"
        }else{
            return directoryPath
        }    
    }

    static List findLocalFileOfChangedClass(String project, String className, String commit){
        
        List filesPath=[]
        File fileDir=new File(project)
        String newClassName = className.replaceAll('\\.','\\/')
        fileDir.eachDirRecurse() { dir ->  
            dir.eachFileMatch(~/.*.java/) { file ->  
                if (file.path.contains(newClassName+".java")){
                    filesPath.add(file.path  )
                }
            }  
        }
        return filesPath
    }

    static synchronized File createSpreadsheet(Path path, String name, String header) {
        File spreadsheet = path.resolve("${name}.csv").toFile()
        if (!spreadsheet.exists())
            appendLineToFile(spreadsheet, header)

        return spreadsheet
    }

    static synchronized void appendLineToFile(File file, String line) {
        file << "${line}\n"
    }
}