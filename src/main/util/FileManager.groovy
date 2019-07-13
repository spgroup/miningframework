package main.util

@Grab(group='commons-io', module='commons-io', version='2.6')
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
        
        File target = new File("${SHA}.java")
        gitCatFile.getInputStream().eachLine {
            target << "${it}\n"
        }
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
        if (!outputDir.exists())
            outputDir.mkdirs()
        
        createStatisticsFiles(outputPath)
        createExperimentalDataFiles(outputPath, false)
        if(createLinksFile)
            createExperimentalDataFiles(outputPath, true)

        return outputDir
    }

    private static File createStatisticsFiles(String outputPath) {
        File statisticsDir = new File(outputPath + '/statistics')
        if (!statisticsDir.exists())
            statisticsDir.mkdirs()

        File statisticsResultsFile = new File(outputPath + "/statistics/results.csv")
        if (statisticsResultsFile.exists())
            statisticsResultsFile.delete()

        statisticsResultsFile << 'project,merge commit,is octopus,number of developers\' mean,number of commits\' mean,number of changed files\' mean, number of changed lines\' mean,duration mean,conclusion delay\n'

        return statisticsResultsFile
    }

    private static File createExperimentalDataFiles(String outputPath, boolean containsLinks) {
        File dataDir = new File(outputPath + '/data')
        if (!dataDir.exists())
            dataDir.mkdirs()        

        File dataResultsFile = new File(outputPath + '/data/results' + ((containsLinks) ? '-links' : '') + '.csv')
        if(dataResultsFile.exists())
            dataResultsFile.delete()

        dataResultsFile << 'project;merge commit;class;method;left modifications;left deletions;right modifications;right deletions\n'
        return dataResultsFile
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

}