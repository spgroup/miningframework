import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class StatisticsCollectorImpl extends StatisticsCollector {

    @Override
    public void collectStatistics() {
        resultsFile = new File("${outputPath}/statistics/results.csv")

        boolean isOctopus = mergeCommit.isOctopus()
        int numberOfMergeConflicts = getNumberOfMergeConflicts()
        boolean mergeConflictOcurrence = numberOfMergeConflicts > 0
        int numberOfConflictingFiles = getNumberOfConflictingFiles()
        double numberOfDevelopersMean = getNumberOfDevelopersMean()
        double numberOfCommitsMean = getNumberOfCommitsMean()
        double numberOfChangedFilesMean = getNumberOfChangedFilesMean()
        double numberOfChangedLinesMean = getNumberOfChangedLinesMean()
        double durationMean = getDurationMean()
        int conclusionDelay = getConclusionDelay()

        resultsFile << "${project.getName()},${mergeCommit.getSHA()},${isOctopus},${numberOfMergeConflicts},${mergeConflictOcurrence},${numberOfConflictingFiles},${numberOfDevelopersMean},${numberOfCommitsMean},${numberOfChangedFilesMean},${numberOfChangedLinesMean},${durationMean},${conclusionDelay}\n"
        println "Statistics collection finished!"
    }

    private int getNumberOfMergeConflicts() {
        int numberOfMergeConflicts = 0

        Process gitShow = ProcessRunner.runProcess(project.getPath(), 'git', 'show', mergeCommit.getSHA())
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

        Process gitShow = ProcessRunner.runProcess(project.getPath(), 'git', 'show', mergeCommit.getSHA())
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
            Process gitRevList = ProcessRunner.runProcess(project.getPath(), 'git', 'rev-list', mergeCommit.getAncestorSHA(), parents[i], '--pretty=%an')

            Set<String> developers = new HashSet<String>()
            gitRevList.getInputStream().eachLine {
                if (!it.startsWith('commit'))
                    developers.add(it)
            }
            numberOfDevelopers[i] = developers.size()
        }
        return geometricMean(numberOfDevelopers)
    }

    private double getNumberOfCommitsMean() {
        String[] parents = mergeCommit.getParentsSHA()
        int[] numberOfCommits = new int[parents.length]

        for (int i = 0; i < parents.length; i++) {
            Process gitRevList = ProcessRunner.runProcess(project.getPath(), 'git', 'rev-list', '--count', mergeCommit.getAncestorSHA(), parents[i])

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
            Process gitRevList = ProcessRunner.runProcess(project.getPath(), 'git', 'diff', '--name-only', parents[i], mergeCommit.getAncestorSHA())

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
            Process gitRevList = ProcessRunner.runProcess(project.getPath(), 'git', 'diff', parents[i], mergeCommit.getAncestorSHA())

            numberOfChangedLines[i] = 0
            gitRevList.getInputStream().eachLine {
                if(it.startsWith('+ ') || it.startsWith('- ')) {
                    numberOfChangedLines[i]++
                }
            }
        }
        return geometricMean(numberOfChangedLines)
    }

    private double getDurationMean() {
        String[] parents = mergeCommit.getParentsSHA()
        int[] numberOfDaysPassed = new int[parents.length]
        SimpleDateFormat formatter = new SimpleDateFormat('yyyy-mm-dd')

        for (int i = 0; i < parents.length; i++) {
            Process gitLog = ProcessRunner.runProcess(project.getPath(), 'git', 'log', '--date=short', '--pretty=%H%n%ad', parents[i])

            numberOfDaysPassed[i] = 0
            BufferedReader reader = new BufferedReader(new InputStreamReader(gitLog.getInputStream()))
            reader.readLine()
            Date parentDate = formatter.parse(reader.readLine())

            String line
            while((line = reader.readLine()) != null) {
                if(line.equals(mergeCommit.getAncestorSHA()))
                    break
            }

            Date ancestorDate = formatter.parse(reader.readLine())
            long diff = parentDate.getTime() - ancestorDate.getTime()
            numberOfDaysPassed[i] = Math.abs(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS))
        }
        return geometricMean(numberOfDaysPassed)
    }

    private int getConclusionDelay() {
        String[] parents = mergeCommit.getParentsSHA()
        Date[] commitDates = new Date[parents.length]
        SimpleDateFormat formatter = new SimpleDateFormat('yyyy-mm-dd')

        // This metric implies two parents only.
        for (int i = 0; i < 2; i++) {
            Process gitShow = ProcessRunner.runProcess(project.getPath(), 'git', 'show', '--date=short', '--pretty=%ad', parents[i])

            BufferedReader reader = new BufferedReader(new InputStreamReader(gitShow.getInputStream()))
            commitDates[i] = formatter.parse(reader.readLine())
            reader.close()
        }
        long diff = Math.abs(commitDates[1].getTime() - commitDates[0].getTime())
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }

    private double geometricMean(int[] array) {
        double product = 1
        for (number in array)
            product *= number
        return Math.pow(product, 1/array.length)
    }
}