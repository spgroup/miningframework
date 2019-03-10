class StatisticsCollectorImpl extends StatisticsCollector {

    public StatisticsCollectorImpl() {
        resultsFile = new File("output/statistics/results.csv")
        if(resultsFile.exists())
            resultsFile.delete()
        resultsFile << 'project,merge commit,number of merge conflicts,merge conflict ocurrence,number of conflicting files, number of developers\' mean,number of commits\' mean,number of changed files\' mean, number of changed lines\' mean,\n'
    }

    @Override
    public void collectStatistics() {
        boolean isOctopus = mergeCommit.isOctopus()
        int numberOfMergeConflicts = getNumberOfMergeConflicts()
        boolean mergeConflictOcurrence = numberOfMergeConflicts > 0
        int numberOfConflictingFiles = getNumberOfConflictingFiles()
        double numberOfDevelopersMean = getNumberOfDevelopersMean()
        double numberOfCommitsMean = getNumberOfCommitsMean()
        double numberOfChangedFilesMean = getNumberOfChangedFilesMean()
        double numberOfChangedLinesMean = getNumberOfChangedLinesMean()

        resultsFile << "${project.getName()},${mergeCommit.getSHA()},${isOctopus},${numberOfMergeConflicts},${mergeConflictOcurrence},${numberOfConflictingFiles},${numberOfDevelopersMean},${numberOfCommitsMean},${numberOfChangedFilesMean},${numberOfChangedLinesMean}\n"
        println "Statistics collection finished!"
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

    private double getNumberOfChangedFilesMean() {
        String[] parents = mergeCommit.getParentsSHA()
        int[] numberOfChangedFiles = new int[parents.length]

        for (int i = 0; i < parents.length; i++) {
            Process gitRevList = new ProcessBuilder('git', 'diff', '--name-only', parents[i], mergeCommit.getAncestorSHA())
                .directory(new File(project.getPath()))
                .start()

            numberOfChangedFiles[i] = 0
            gitRevList.getInputStream().eachLine {
                numberOfChangedFiles[i]++
            }
        }
        return geometricMean(numberOfChangedFiles)
    }

    private double getNumberOfChangedLinesMean() {
        String[] parents = mergeCommit.getParentsSHA()
        int[] numberOfChangedLines = new int[parents.length]

        for (int i = 0; i < parents.length; i++) {
            Process gitRevList = new ProcessBuilder('git', 'diff', parents[i], mergeCommit.getAncestorSHA())
                .directory(new File(project.getPath()))
                .start()

            numberOfChangedLines[i] = 0
            gitRevList.getInputStream().eachLine {
                if(it.startsWith('+ ') || it.startsWith('- ')) {
                    numberOfChangedLines[i]++
                }
            }
        }
        return geometricMean(numberOfChangedLines)
    }

    private double geometricMean(int[] array) {
        double product = 1
        for (number in array)
            product *= number
        return Math.pow(product, 1/array.length)
    }
}