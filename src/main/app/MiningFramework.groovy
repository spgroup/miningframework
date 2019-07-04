package main.app

@Grab('com.xlson.groovycsv:groovycsv:1.3')
@Grab('com.google.inject:guice:4.2.2')
import static com.xlson.groovycsv.CsvParser.parseCsv
import com.google.inject.*
import java.io.File
import java.util.ArrayList
import java.text.SimpleDateFormat
import static groovy.io.FileType.DIRECTORIES
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

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
    private OutputProcessor outputProcessor

    static public Arguments arguments
    private final String LOCAL_PROJECT_PATH = 'localProject'
    private final String LOCAL_RESULTS_REPOSITORY_PATH = System.getProperty('user.home')
    
    @Inject
    public MiningFramework(ExperimentalDataCollector dataCollector, StatisticsCollector statCollector, CommitFilter commitFilter, ProjectProcessor projectProcessor, OutputProcessor outputProcessor) {
        this.dataCollector = dataCollector
        this.statCollector = statCollector
        this.commitFilter = commitFilter
        this.projectProcessor = projectProcessor
        this.outputProcessor = outputProcessor
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

        // TODO: Change logic to move projects to BlockingQueue
        BlockingQueue<Project> projectQueue = new LinkedBlockingQueue<Project>()
        for (Project project : projectList ) {    
            projectQueue.add(project)
        }

        def worker = new MiningWorker(dataCollector, statCollector, commitFilter, projectQueue, LOCAL_PROJECT_PATH)

        worker.run()
        processOutput()
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

    private ArrayList<Project> processProjects(ArrayList<Project> projects) {
        return projectProcessor.processProjects(projects)
    }

    private void processOutput() {
        outputProcessor.processOutput()
    }

    private void printPushInformation(String url) {
        println "Proceeding to push output files to ${url}."
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
