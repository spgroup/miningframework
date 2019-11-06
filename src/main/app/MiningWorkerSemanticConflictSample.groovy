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

class MiningWorkerSemanticConflictSample extends MiningWorker {

    public MiningWorkerSemanticConflictSample(Set<DataCollector> dataCollectors, CommitFilter commitFilter, BlockingQueue<Project> projectList, String baseDir) {
        super(dataCollectors, commitFilter, projectList, baseDir)
    }

    @Override
    protected void cloneRepository(Project project, String target) {    
        target =  java.nio.file.Paths.get(target, project.getName()).toString()
        println "Cloning repository ${project.getName()} into ${target}"

        String current_path = System.getProperty("user.dir");
        File projectDirectory = new File(target)
        if(projectDirectory.exists()) {
            FileManager.delete(projectDirectory)
        }
        projectDirectory.mkdirs()
        
        String url = project.getPath()

        if (arguments.providedAccessKey()) {
            String token = arguments.getAccessKey();
            String[] projectOwnerAndName = project.getOwnerAndName()
            url = "https://${token}@github.com/${projectOwnerAndName[0]}/${projectOwnerAndName[1]}"
        }

        ProcessBuilder builder = ProcessRunner.buildProcess('./', 'git', 'clone', url, target)
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    
        Process process = ProcessRunner.startProcess(builder)
        process.waitFor()
        project.setFullLocalPath(java.nio.file.Paths.get(current_path, target).toString())
        project.setPath(target)
    }

}