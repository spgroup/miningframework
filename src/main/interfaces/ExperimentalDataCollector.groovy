package main.interfaces

import main.project.*

public abstract class ExperimentalDataCollector {

    private Project project
    private MergeCommit mergeCommit
    private File resultsFile
    private static String outputPath

    public abstract void collectExperimentalData()

    public Project getProject() {
        return project
    }

    public setProject(Project project) {
        this.project = project
    }

    public MergeCommit getMergeCommit() {
        return mergeCommit
    }

    public setMergeCommit(MergeCommit mergeCommit) {
        this.mergeCommit = mergeCommit
    }

    public getResultsFile() {
        return resultsFile
    }

    public setResultsFile(File resultsFile) {
        this.resultsFile = resultsFile
    }

    public setOutputPath(String outputPath) {
        this.outputPath = outputPath
    }

    public String getOutputPath() {
        return this.outputPath
    }
}