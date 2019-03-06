public abstract class StatisticsCollector {
    
    private Project project
    private MergeCommit mergeCommit
    private File resultsFile
    
    public abstract void collectStatistics()

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
}