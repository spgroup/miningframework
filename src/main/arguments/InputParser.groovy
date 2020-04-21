package arguments

import exception.InvalidArgsException
import project.Project

import static com.xlson.groovycsv.CsvParser.parseCsv
import static groovy.io.FileType.DIRECTORIES

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
        if(projectList.size() == 0) throw new InvalidArgsException('The input file cannot be processed, the project list will be considered as empty')
        return projectList
    }

    @Deprecated
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
} 