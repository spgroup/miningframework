package app

import com.google.inject.Inject

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

import arguments.*
import project.*
import interfaces.*
import exception.UnstagedChangesException

class MiningFramework {

    private ArrayList<Project> projectList
   
    private Set<DataCollector> dataCollectors
    private CommitFilter commitFilter
    private Set<ProjectProcessor> projectProcessors
    private Set<OutputProcessor> outputProcessors

    static public Arguments arguments
    private final String LOCAL_PROJECT_PATH = 'clonedRepositories'

    @Inject
    MiningFramework(Set<DataCollector> dataCollectors, CommitFilter commitFilter,
                    Set<ProjectProcessor> projectProcessors, Set<OutputProcessor> outputProcessor) {
        this.dataCollectors = dataCollectors
        this.commitFilter = commitFilter
        this.projectProcessors = projectProcessors
        this.outputProcessors = outputProcessor
    }

    void start() {
        try {
            println "#### MINING STARTED ####"

            for (ProjectProcessor projectProcessor : projectProcessors) {
                projectList = projectProcessor.processProjects(projectList)
            }

            BlockingQueue<Project> projectQueue = populateProjectsQueue(projectList)
            
            Thread [] workers = createAndStartMiningWorkers (projectQueue)

            waitForMiningWorkers(workers)

            for (OutputProcessor outputProcessor : outputProcessors) {
                outputProcessor.processOutput()
            }

            println "#### MINING FINISHED ####"
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

    void setProjectList(ArrayList<Project> projectList) {
        this.projectList = projectList
    }

    void setArguments(Arguments arguments) {
        this.arguments = arguments
    }

}
