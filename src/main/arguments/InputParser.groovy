package main.arguments

@Grab('com.xlson.groovycsv:groovycsv:1.3')
import static groovy.io.FileType.DIRECTORIES
import static com.xlson.groovycsv.CsvParser.parseCsv

import main.project.*;

class InputParser {

    static ArrayList<Project> getProjectList(String inputPath) {
        ArrayList<Project> projectList = new ArrayList<Project>()

        String projectsFile = new File(inputPath).getText()
        def iterator = parseCsv(projectsFile)

        for (line in iterator) {
            Map lineMap = line.toMap()

            String path = line["path"]

            if (lineMap.containsKey("name")) {
                String name = line["name"]
                
                projectList.add(new Project(name, path))
            } else {
                projectList.add(new Project(path))
            }
        }

        return projectList
    }

    private static ArrayList<Project> getProjects(String directoryName, String directoryPath) {
        ArrayList<Project> projectList = new ArrayList<Project>()

        File directory = new File(directoryPath)
        directory.traverse(type: DIRECTORIES, maxDepth: 0) {
             
             // Checking if it's a git project.
             String filePath = it.toString()
             if(new File("${filePath}/.git").exists()) {
                 Project project = new Project("${directoryName}/${it.getName()}", filePath)
                 projectList.add(project)
             }
        }

        return projectList
    }

    private static ArrayList<CommitPair> getCommitPairsByProject(String commitPairFilePath, String projectName) {
        ArrayList<CommitPair> commitPairs = new ArrayList<CommitPair>()

        String commitPairFile = new File(commitPairFilePath).getText()
        def iterator = parseCsv(commitPairFile)
        
        for (line in iterator) {
            if (line[0] == projectName) {
                if (line[4] != "" && line[5] != ""){
                    CommitPair commitPair = new CommitPair(line[2], line[3], line[4], line[5])
                    commitPairs.add(commitPair)
                }else{
                    CommitPair commitPair = new CommitPair(line[2], line[3])
                    commitPairs.add(commitPair)
                }
            }
        }
        return commitPairs
    }
} 