@Grab('com.xlson.groovycsv:groovycsv:1.3')
import static com.xlson.groovycsv.CsvParser.parseCsv
import java.io.File
import java.util.ArrayList;

class MiningFramework {

    private ArrayList<Project> projectList

    public MiningFramework(ArrayList<Project> projectList) {
        this.projectList = projectList
    }

    public void start() {
        
    }

    static main(args) {
        printStartAnalysis()

        ArrayList<Project> projectList = getProjectList()
        MiningFramework framework = new MiningFramework(projectList)
        framework.start()

        printFinishAnalysis()
    }

    static ArrayList<Project> getProjectList() {
        ArrayList<Project> projectList = new ArrayList<Project>()

        String projectsFile = new File('../../projects.csv').getText()
        def iterator = parseCsv(projectsFile)
        for (line in iterator) {
            String name = line[0]
            String path = line[1]
            Project project = new Project(name, path)

            projectList.add(project)
        }

        return projectList
    }

    static void printStartAnalysis() {
        println "#### MINING STARTED ####\n"
    }

    static void printFinishAnalysis() {
        println "\n#### MINING FINISHED ####"
    }

}
