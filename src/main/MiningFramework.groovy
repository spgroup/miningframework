@Grab('com.xlson.groovycsv:groovycsv:1.3')
import static com.xlson.groovycsv.CsvParser.parseCsv
import java.io.File
import java.nio.file.Files 
import java.nio.file.Paths
import java.util.ArrayList;

class MiningFramework {

    private ArrayList<Project> projectList
    private final String LOCAL_PROJECT_PATH = 'localProject'

    public MiningFramework(ArrayList<Project> projectList) {
        this.projectList = projectList
    }

    public void start() {
        for (project in projectList) {
            if (project.isRemote())
                cloneRepository(project)
        }
    }

    private void cloneRepository(Project project) {

        println "Cloning repository ${project.getName()} into ${LOCAL_PROJECT_PATH}"

        if(Files.exists(Paths.get(LOCAL_PROJECT_PATH))) {
            File projectDirectory = new File(LOCAL_PROJECT_PATH)
            delete(projectDirectory)
        }

        Process gitClone = new ProcessBuilder('git', 'clone', project.getPath(), LOCAL_PROJECT_PATH).start()
        gitClone.waitFor()
        
    }

    private delete(File file) {
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

    static main(args) {
        printStartAnalysis()

        ArrayList<Project> projectList = getProjectList()
        MiningFramework framework = new MiningFramework(projectList)
        framework.start()

        printFinishAnalysis()
    }

    static ArrayList<Project> getProjectList() {
        ArrayList<Project> projectList = new ArrayList<Project>()

        String projectsFile = new File('projects.csv').getText()
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
