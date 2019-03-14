@Grab('com.xlson.groovycsv:groovycsv:1.3')
@Grab('com.google.inject:guice:4.2.2')
import static com.xlson.groovycsv.CsvParser.parseCsv
import com.google.inject.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Files 
import java.nio.file.Paths
import java.util.ArrayList;
import static groovy.io.FileType.DIRECTORIES

class MiningFramework {

    static private ArgsManager argsManager
    private ArrayList<Project> projectList
   
    private StatisticsCollector statCollector
    private DataCollector dataCollector
    private CommitFilter commitFilter

    private final String LOCAL_PROJECT_PATH = 'localProject'
    
    @Inject
    public MiningFramework(DataCollector dataCollector, StatisticsCollector statCollector, CommitFilter commitFilter) {
        this.dataCollector = dataCollector
        this.statCollector = statCollector
        this.commitFilter = commitFilter
    }

    public void start() {
        dataCollector.setOutputPath(argsManager.getOutputPath())
        statCollector.setOutputPath(argsManager.getOutputPath())

        for (project in projectList) {
            printProjectInformation(project)
            if (project.isRemote())
                cloneRepository(project)
            
            ArrayList<MergeCommit> mergeCommits = project.getMergeCommits(argsManager.getSinceDate(), argsManager.getUntilDate()) // Since date and until date as arguments (dd/mm/yyyy).
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

        File projectDirectory = new File(LOCAL_PROJECT_PATH)
        if(projectDirectory.exists()) {
            FileManager.delete(projectDirectory)
        }
        projectDirectory.mkdirs()

        Process gitClone = new ProcessBuilder('git', 'clone', project.getPath(), LOCAL_PROJECT_PATH).start()
        gitClone.waitFor()
        project.setPath(LOCAL_PROJECT_PATH)
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
            FileManager.delete(new File(LOCAL_PROJECT_PATH))
    }

    public void setProjectList(ArrayList<Project> projectList) {
        this.projectList = projectList
    }

    static main(args) {
        argsManager = new ArgsManager()
        try {
            argsManager.parse(args)
            
            if (argsManager.isHelp())
                return
    
            FileManager.createOutputFiles(argsManager.getOutputPath())
        
            printStartAnalysis()
     
            ArrayList<Project> projectList = getProjectList()
        
            Class injectorClass = argsManager.getInjector()

            Injector injector = Guice.createInjector(injectorClass.newInstance())
            MiningFramework framework = injector.getInstance(MiningFramework.class)

            framework.setProjectList(projectList)
            framework.start()

            printFinishAnalysis()
        } catch (InvalidArgsException e) {
            println e.message
            println 'Run the miningframework with --help to see the possible arguments'
            return
        }
    }

    static ArrayList<Project> getProjectList() {
        ArrayList<Project> projectList = new ArrayList<Project>()

        String projectsFile = new File(argsManager.getInputPath()).getText()
        def iterator = parseCsv(projectsFile)
        for (line in iterator) {
            String name = line[0]
            String path = line[1]

            boolean relativePath
            try {
                relativePath = line[2]
            } catch(ArrayIndexOutOfBoundsException e) {
                relativePath = false
            }

            if(relativePath) 
                projectList.addAll(getProjects(name, path))
            else {
                Project project = new Project(name, path)
                projectList.add(project)
            }
        }

        return projectList
    }

    static ArrayList<Project> getProjects(String directoryName, String directoryPath) {
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

    static void printStartAnalysis() {
        println "#### MINING STARTED ####\n"
    }

    static void printFinishAnalysis() {
        println "#### MINING FINISHED ####"
    }

}
