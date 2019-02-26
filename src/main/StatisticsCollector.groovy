class StatisticsCollector {

    private Project project
    private MergeCommit mergeCommit

    public void collectStatistics() {
        println "Analyzing merge commit ${mergeCommit.getSHA()}"

        int numberOfMergeConflicts = getNumberOfMergeConflicts()
        boolean mergeConflictOcurrence = numberOfMergeConflicts > 0
        int numberOfConflictingFiles = getNumberOfConflictingFiles()
        double numberOfDevelopersMean = getNumberOfDevelopersMean()
        double numberOfCommitsMean = getNumberOfCommitsMean()

        println "Number of merge conflicts: ${numberOfMergeConflicts}"
        println "Number of conflicting files: ${numberOfConflictingFiles}"
        println "Geometric mean of the number of developers: ${numberOfDevelopersMean}"
        println "Geometric mean of the number of commits: ${numberOfCommitsMean}"
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

    private double getNumberOfDevelopersMean() {
        String[] parents = mergeCommit.getParentsSHA()
        int[] numberOfDevelopers = new int[parents.length]

        for (int i = 0; i < parents.length; i++) {
            Process gitRevList = new ProcessBuilder('git', 'rev-list', mergeCommit.getAncestorSHA(), parents[i], '--header')
                .directory(new File(project.getPath()))
                .start()

            gitRevList.getInputStream().eachLine {
                if (it.contains('author') || it.contains('co-authored-by'))
                    numberOfDevelopers[i]++ // Devemos filtrar os autores?
            }
        }
        return geometricMean(numberOfDevelopers)
    }

    private double getNumberOfCommitsMean() {
        String[] parents = mergeCommit.getParentsSHA()
        int[] numberOfCommits = new int[parents.length]

        for (int i = 0; i < parents.length; i++) {
            Process gitRevList = new ProcessBuilder('git', 'rev-list', '--count', mergeCommit.getAncestorSHA(), parents[i])
                .directory(new File(project.getPath()))
                .start()

            gitRevList.getInputStream().eachLine {
                numberOfCommits[i] = Integer.parseInt(it)
            }
        }
        return geometricMean(numberOfCommits)
    }

    private double geometricMean(int[] array) {
        double product = 1
        for (number in array)
            product *= number
        return Math.pow(product, 1/array.length)
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