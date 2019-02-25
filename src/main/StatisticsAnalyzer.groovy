class StatisticsAnalyzer {

    private Project project
    private MergeCommit mergeCommit

    public void collectStatistics() {
        println "Analyzing merge commit ${mergeCommit.getSHA()}"

        int numberOfMergeConflicts = getNumberOfMergeConflicts()
        boolean mergeConflictOcurrence = numberOfMergeConflicts > 0
        int numberOfConflictingFiles = getNumberOfConflictingFiles()

        println "Number of merge conflicts: ${numberOfMergeConflicts}"
        println "Number of conflicting files: ${numberOfConflictingFiles}"
    }

    private int getNumberOfMergeConflicts() {
        int numberOfMergeConflicts = 0

        Process gitShow = new ProcessBuilder('git', 'show', mergeCommit.getSHA())
            .directory(new File(project.getPath()))
            .start()

        BufferedReader reader = new BufferedReader(new InputStreamReader(gitShow.getInputStream()))
        String line
        while((line = reader.readLine()) != null) {
            if(line.endsWith("======="))
                numberOfMergeConflicts++
        }

        return numberOfMergeConflicts
    }

    private int getNumberOfConflictingFiles() {
        int numberOfConflictingFiles = 0

        Process gitShow = new ProcessBuilder('git', 'show', mergeCommit.getSHA())
            .directory(new File(project.getPath()))
            .start()

        BufferedReader reader = new BufferedReader(new InputStreamReader(gitShow.getInputStream()))
        String line
        boolean fileHasConflict = false
        while((line = reader.readLine()) != null) {
            if(line.startsWith('diff --cc') && fileHasConflict)
                numberOfConflictingFiles++
            else if(line.startsWith('diff --cc')) {
                fileHasConflict = false
            }

            if(line.endsWith("======="))
                fileHasConflict = true
        }

        if(fileHasConflict)
            numberOfConflictingFiles++

        return numberOfConflictingFiles
    }

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
}