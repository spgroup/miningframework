package main.util

import java.nio.file.Files 
import java.nio.file.Paths
import java.nio.file.Path
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import org.apache.commons.io.FileUtils 

import main.project.*

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
}