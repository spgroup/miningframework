package main.app

import java.text.SimpleDateFormat
import java.io.File

import main.interfaces.*
import static main.app.MiningFramework.arguments
import java.util.concurrent.BlockingQueue


import main.arguments.*
import main.project.*
import main.interfaces.*
import main.exception.InvalidArgsException
import main.exception.UnstagedChangesException
import main.exception.UnexpectedPostScriptException
import main.exception.NoAccessKeyException
import main.util.*

class MiningWorkerSemanticConflictSampleDynamic extends MiningWorker {

    public MiningWorkerSemanticConflictSampleDynamic(Set<DataCollector> dataCollectors, CommitFilter commitFilter, BlockingQueue<Project> projectList, String baseDir) {
        super(dataCollectors, commitFilter, projectList, baseDir)
    }

    @Override
    protected void cloneRepository(Project project, String target) {    
        String current_path = System.getProperty("user.dir");
        super.cloneRepository(project,target)
        project.setFullLocalPath(java.nio.file.Paths.get(current_path, target).toString())
    }

}