@Grab('com.xlson.groovycsv:groovycsv:1.3')
@Grab('com.google.inject:guice:4.2.2')
import static com.xlson.groovycsv.CsvParser.parseCsv
import com.google.inject.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Files 
import java.nio.file.Paths
import java.util.ArrayList
import static groovy.io.FileType.DIRECTORIES

class MiningFramework {

    private ArrayList<Project> projectList
   
    private StatisticsCollector statCollector
    private DataCollector dataCollector
    private CommitFilter commitFilter
    static public Arguments arguments
    private final String LOCAL_PROJECT_PATH = 'localProject'
    private final String LOCAL_RESULTS_REPOSITORY_PATH = System.getProperty('user.home')
    
    @Inject
    public MiningFramework(DataCollector dataCollector, StatisticsCollector statCollector, CommitFilter commitFilter) {
        this.dataCollector = dataCollector
        this.statCollector = statCollector
        this.commitFilter = commitFilter
    }

    public void start() {
        dataCollector.setOutputPath(arguments.getOutputPath())
        statCollector.setOutputPath(arguments.getOutputPath())

        for (project in projectList) {
            printProjectInformation(project)
            if (project.isRemote())
                cloneRepository(project, LOCAL_PROJECT_PATH)
            
            ArrayList<MergeCommit> mergeCommits = project.getMergeCommits(arguments.getSinceDate(), arguments.getUntilDate()) // Since date and until date as arguments (dd/mm/yyyy).
            for (mergeCommit in mergeCommits) {
                if (applyFilter(project, mergeCommit)) {
                    printMergeCommitInformation(mergeCommit)
                    collectStatistics(project, mergeCommit)
                    collectData(project, mergeCommit)
                }
            }

            if(!arguments.getResultsRemoteRepository().equals('')) // Will push.
                pushResults(project, arguments.getResultsRemoteRepository())

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

    private void pushResults(Project project, String remoteRepositoryURL) {
        Project resultsRepository = new Project('', remoteRepositoryURL)
        printPushInformation(remoteRepositoryURL)
        String targetPath = "${LOCAL_RESULTS_REPOSITORY_PATH}/resultsRepository"
        cloneRepository(resultsRepository, targetPath)

        // Copy output files, add, commit and then push.
        FileManager.copyDirectory(arguments.getOutputPath(), "${targetPath}/output-${project.getName()}")
        Process gitAdd = ProcessRunner.runProcess(targetPath, 'git', 'add', '.')
        gitAdd.waitFor()
        
        Process gitCommit = ProcessRunner.runProcess(targetPath, 'git', 'commit', '-m', "Analysed project ${project.getName()}")
        gitCommit.waitFor()
        gitCommit.getInputStream().eachLine {
            println it
        }

        Process gitPush = ProcessRunner.runProcess(targetPath, 'git', 'push', '--force-with-lease')
        gitPush.waitFor()
        gitPush.getInputStream().eachLine {
            println it
        }

        FileManager.delete(new File(targetPath))
    }

    private void cloneRepository(Project project, String target) {

        println "Cloning repository ${project.getName()} into ${target}"

        File projectDirectory = new File(target)
        if(projectDirectory.exists()) {
            FileManager.delete(projectDirectory)
        }
        projectDirectory.mkdirs()

        Process gitClone = new ProcessBuilder('git', 'clone', project.getPath(), target).start()
        gitClone.waitFor()
        project.setPath(target)
    }

    private void printProjectInformation(Project project) {
        println "PROJECT: ${project.getName()}"
    }

    private void printMergeCommitInformation(MergeCommit mergeCommit) {
        println "Merge commit: ${mergeCommit.getSHA()}"
    }

    private void printPushInformation(String url) {
        println "Proceeding to push output files to ${url}."
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
        ArgsParser argsParser = new ArgsParser()
        try {
            Arguments appArguments = argsParser.parse(args)
            
            if (!appArguments.isHelp()) {
                Class injectorClass = appArguments.getInjector()
                Injector injector = Guice.createInjector(injectorClass.newInstance())
                MiningFramework framework = injector.getInstance(MiningFramework.class)

                framework.setArguments(appArguments)

                FileManager.createOutputFiles(appArguments.getOutputPath(), !appArguments.getResultsRemoteRepository().equals(''))
            
                printStartAnalysis()                
                
                ArrayList<Project> projectList = getProjectList()
                framework.setProjectList(projectList)
                framework.start()

                printFinishAnalysis()
            }
    
        } catch (InvalidArgsException e) {
            println e.message
            println 'Run the miningframework with --help to see the possible arguments'
            return
        }
    }

    static ArrayList<Project> getProjectList() {
        ArrayList<Project> projectList = new ArrayList<Project>()

        String projectsFile = new File(arguments.getInputPath()).getText()
        def iterator = parseCsv(projectsFile)
        for (line in iterator) {
            String name = line[0]
            String path = line[1]

            boolean relativePath
            try {
                relativePath = line[2].equals("true")
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

    void setArguments(Arguments arguments) {
        this.arguments = arguments
    }

    static Arguments getArguments() {
        return arguments
    }

    static void printStartAnalysis() {
        println "#### MINING STARTED ####\n"
    }

    static void printFinishAnalysis() {
        println "#### MINING FINISHED ####"
    }

}
