@Grab('com.xlson.groovycsv:groovycsv:1.3')
import static com.xlson.groovycsv.CsvParser.parseCsv
import java.io.File
import java.nio.file.Files 
import java.nio.file.Paths
import java.util.ArrayList;

class MiningFramework {

    private ArrayList<Project> projectList
    private StatisticsCollector statCollector = new StatisticsCollector()
    private DataCollector dataCollector = new DataCollector()
    private CommitFilter commitFilter = new CommitFilter()
    private final String LOCAL_PROJECT_PATH = 'localProject'

    public MiningFramework(ArrayList<Project> projectList) {
        this.projectList = projectList
    }

    public void start() {
        for (project in projectList) {
            printProjectInformation(project)
            if (project.isRemote())
                cloneRepository(project)
            
            ArrayList<MergeCommit> mergeCommits = project.getMergeCommits("01/12/2018", '') // Since date and until date as arguments (dd/mm/yyyy).
            for (mergeCommit in mergeCommits) {
                if (applyFilter(project, mergeCommit)) {
                    printMergeCommitInformation(mergeCommit)
                    collectStatistics(project, mergeCommit)
                    collectData(project, mergeCommit)
                }
            }
            endProjectAnalysis()
        }
    }

    private boolean applyFilter(Project project, MergeCommit mergeCommit) {
        commitFilter.setProject(project)
        commitFilter.setMergeCommit(mergeCommit)
        return commitFilter.applyFilter()
    }

    private void collectStatistics(Project project, MergeCommit mergeCommit) {
        statCollector.setProject(project)
        statCollector.setMergeCommit(mergeCommit)
        statCollector.collectStatistics()
    }

    private void collectData(Project project, MergeCommit mergeCommit) {
        dataCollector.setProject(project)
        dataCollector.setMergeCommit(mergeCommit)
        dataCollector.collectData()
    }

    private void cloneRepository(Project project) {

        println "Cloning repository ${project.getName()} into ${LOCAL_PROJECT_PATH}"

        if(Files.exists(Paths.get(LOCAL_PROJECT_PATH))) {
            File projectDirectory = new File(LOCAL_PROJECT_PATH)
            delete(projectDirectory)
        }

        Process gitClone = new ProcessBuilder('git', 'clone', project.getPath(), LOCAL_PROJECT_PATH).start()
        gitClone.waitFor()
        project.setPath(LOCAL_PROJECT_PATH)
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

    private void printProjectInformation(Project project) {
        println "PROJECT: ${project.getName()}"
    }

    private void printMergeCommitInformation(MergeCommit mergeCommit) {
        println "Merge commit: ${mergeCommit.getSHA()}"
    }

    private void endProjectAnalysis() {
        File projectDirectory = new File(LOCAL_PROJECT_PATH)
        if (projectDirectory.exists())
            delete(new File(LOCAL_PROJECT_PATH))
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
        println "#### MINING FINISHED ####"
    }

}
