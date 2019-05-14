package main.app

@Grab('com.xlson.groovycsv:groovycsv:1.3')
@Grab('com.google.inject:guice:4.2.2')
import static com.xlson.groovycsv.CsvParser.parseCsv
import com.google.inject.*
import java.io.File
import java.util.ArrayList
import java.text.SimpleDateFormat
import static groovy.io.FileType.DIRECTORIES

import main.arguments.*
import main.project.*
import main.interfaces.*
import main.exception.InvalidArgsException
import main.exception.UnstagedChangesException
import main.exception.UnexpectedPostScriptException
import main.exception.NoAccessKeyException
import main.util.*

class MiningFramework {

    private ArrayList<Project> projectList
   
    private StatisticsCollector statCollector
    private ExperimentalDataCollector dataCollector
    private CommitFilter commitFilter
    private ProjectProcessor projectProcessor

    static public Arguments arguments
    private final String LOCAL_PROJECT_PATH = 'localProject'
    private final String LOCAL_RESULTS_REPOSITORY_PATH = System.getProperty('user.home')
    
    @Inject
    public MiningFramework(ExperimentalDataCollector dataCollector, StatisticsCollector statCollector, CommitFilter commitFilter, ProjectProcessor projectProcessor) {
        this.dataCollector = dataCollector
        this.statCollector = statCollector
        this.commitFilter = commitFilter
        this.projectProcessor = projectProcessor
    }

    static main(args) {
        ArgsParser argsParser = new ArgsParser()
        try {
            Arguments appArguments = argsParser.parse(args)
            
            if (appArguments.isHelp()) {
                argsParser.printHelp()
            } else {
                Class injectorClass = appArguments.getInjector()
                Injector injector = Guice.createInjector(injectorClass.newInstance())
                MiningFramework framework = injector.getInstance(MiningFramework.class)

                framework.setArguments(appArguments)

                FileManager.createOutputFiles(appArguments.getOutputPath(), appArguments.isPushCommandActive())
            
                printStartAnalysis()                
                
                ArrayList<Project> projectList = getProjectList()

                framework.setProjectList(projectList)
                framework.start()

                printFinishAnalysis()

                runPostScript()
            }
    
        } catch (InvalidArgsException e) {
            println e.message
            println 'Run the miningframework with --help to see the possible arguments'
        } catch (UnstagedChangesException | UnexpectedPostScriptException | NoAccessKeyException e) {
            println e.message
        }
    }

    public void start() {
        projectList = processProjects(projectList)

        for (project in projectList) {
            printProjectInformation(project)
            
            if (project.isRemote()) {
                cloneRepository(project, LOCAL_PROJECT_PATH)
            } else {
                checkForUnstagedChanges(project);
            }
            
            List<MergeCommit> mergeCommits = project.getMergeCommits(arguments.getSinceDate(), arguments.getUntilDate()) // Since date and until date as arguments (dd/mm/yyyy).
            for (mergeCommit in mergeCommits) {
                if (applyFilter(project, mergeCommit)) {
                    printMergeCommitInformation(mergeCommit)
                    collectStatistics(project, mergeCommit)
                    collectExperimentalData(project, mergeCommit)
                }
            }

            if(arguments.isPushCommandActive()) // Will push.
                pushResults(project, arguments.getResultsRemoteRepositoryURL())
            
            endProjectAnalysis()
        }
    }

    static private void runPostScript() {
        String postScript = arguments.getPostScript()
        if (postScript.length() > 0) {
            println "Executing post script..."
            try {
                String scriptOutput = ProcessRunner.runProcess(".", postScript.split(' ')).getText()
                println scriptOutput
            } catch (IOException e) {
                throw new UnexpectedPostScriptException(e.message)
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new UnexpectedPostScriptException(e.message)
            }
        }
    }

    private void checkForUnstagedChanges(Project project) {
        String gitDiffOutput = ProcessRunner.runProcess(project.getPath(), "git", "diff").getText()

        if (gitDiffOutput.length() != 0) {
            throw new UnstagedChangesException(project.getName())
        }
    }

    private boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return commitFilter.applyFilter(project, mergeCommit)
    }

    private void collectStatistics(Project project, MergeCommit mergeCommit) {
        statCollector.collectStatistics(project, mergeCommit)
    }

    private void collectExperimentalData(Project project, MergeCommit mergeCommit) {
        dataCollector.collectExperimentalData(project, mergeCommit)
    }

    private ArrayList<Project> processProjects(ArrayList<Project> projects) {
        return projectProcessor.processProjects(projects)
    }

    private void pushResults(Project project, String remoteRepositoryURL) {
        Project resultsRepository = new Project('', remoteRepositoryURL)
        printPushInformation(remoteRepositoryURL)
        String targetPath = "${LOCAL_RESULTS_REPOSITORY_PATH}/resultsRepository"
        cloneRepository(resultsRepository, targetPath)

        // Copy output files, add, commit and then push.
        FileManager.copyDirectory(getOutputPath(), "${targetPath}/output-${project.getName()}")
        Process gitAdd = ProcessRunner.runProcess(targetPath, 'git', 'add', '.')
        gitAdd.waitFor()

        def nowDate = new Date()
        def sdf = new SimpleDateFormat("dd/MM/yyyy")
        Process gitCommit = ProcessRunner
            .runProcess(targetPath, 'git', 'commit', '-m', "Analysed project ${project.getName()} - ${sdf.format(nowDate)}")
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
        
        String url = project.getPath()
        String token = arguments.getAccessKey();
        if (token.length() > 0) {
            String[] projectOwnerAndName = project.getOwnerAndName()
            url = "https://${token}@github.com/${projectOwnerAndName[0]}/${projectOwnerAndName[1]}"
        }

        Process gitClone = ProcessRunner.runProcess('./', 'git', 'clone', url, target)
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

    static ArrayList<Project> getProjectList() {
        ArrayList<Project> projectList = new ArrayList<Project>()

        String projectsFile = new File(getInputPath()).getText()
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

    static String getOutputPath() {
        return arguments.getOutputPath()
    }

    static String getInputPath() {
        return arguments.getInputPath()
    }

    static String isPushCommandActive() {
        return arguments.isPushCommandActive()
    }

    static String getResultsRemoteRepositoryURL() {
        return arguments.getResultsRemoteRepositoryURL()
    }


    static void printStartAnalysis() {
        println "#### MINING STARTED ####\n"
    }

    static void printFinishAnalysis() {
        println "#### MINING FINISHED ####"
    }

}
