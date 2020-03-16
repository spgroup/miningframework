package main.app

import com.google.inject.Inject
import java.io.File
import java.util.ArrayList
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

import main.arguments.*
import main.project.*
import main.interfaces.*
import main.exception.InvalidArgsException
import main.exception.UnstagedChangesException

class MiningFramework {

    private ArrayList<Project> projectList
   
    private Set<DataCollector> dataCollectors
    private CommitFilter commitFilter
    private ProjectProcessor projectProcessor
    private OutputProcessor outputProcessor

    static public Arguments arguments
    private final String LOCAL_PROJECT_PATH = 'clonedRepositories'
    private final String LOCAL_RESULTS_REPOSITORY_PATH = System.getProperty('user.home')
    
    @Inject
    public MiningFramework(Set<DataCollector> dataCollectors, CommitFilter commitFilter, ProjectProcessor projectProcessor, OutputProcessor outputProcessor) {
        this.dataCollectors = dataCollectors
        this.commitFilter = commitFilter
        this.projectProcessor = projectProcessor
        this.outputProcessor = outputProcessor
    }

    public void start() {
        try {
            printStartAnalysis()                

            projectList = processProjects(projectList)

            BlockingQueue<Project> projectQueue = populateProjectsQueue(projectList)
            
            Thread [] workers = createAndStartMiningWorkers (projectQueue)

            waitForMiningWorkers(workers)

            processOutput()

            printFinishAnalysis()
        } catch (UnstagedChangesException e) { // framework defined errors
            println e.message;
        }
    }

    BlockingQueue<Project> populateProjectsQueue(List<Project> projectList) {
        BlockingQueue<Project> projectQueue = new LinkedBlockingQueue<Project>()
        
        for (Project project : projectList ) {    
            projectQueue.add(project)
        }
        
        return projectQueue
    }

    Thread [] createAndStartMiningWorkers (BlockingQueue<Project> projectQueue) {
        int numOfThreads = arguments.getNumOfThreads()
        
        Thread [] workers = new Thread[numOfThreads]
        
        for (int i = 0; i < numOfThreads; i++) {
            Runnable worker = new MiningWorker(dataCollectors, commitFilter, projectQueue, LOCAL_PROJECT_PATH);
            workers[i] = new Thread(worker)
            workers[i].start();
        }

        return workers
    }

    void waitForMiningWorkers (Thread[] workers) {
        for (int i = 0; i < workers.length ; i++) {
            workers[i].join();
        }
    }

    private ArrayList<Project> processProjects(ArrayList<Project> projects) {
        return projectProcessor.processProjects(projects)
    }

    private void processOutput() {
        outputProcessor.processOutput()
    }

    public void setProjectList(ArrayList<Project> projectList) {
        this.projectList = projectList
    }

    void setArguments(Arguments arguments) {
        this.arguments = arguments
    }


    static void printStartAnalysis() {
        println "#### MINING STARTED ####\n"
    }

    static void printFinishAnalysis() {
        println "#### MINING FINISHED ####"
    }

}
