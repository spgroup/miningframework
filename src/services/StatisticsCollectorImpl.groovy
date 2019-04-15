package services
import main.interfaces.StatisticsCollector

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import java.text.ParseException 

import main.util.*
import main.project.*
import main.script.MiningFramework
import java.util.regex.Pattern
import java.util.regex.Matcher

class StatisticsCollectorImpl implements StatisticsCollector {

    // Looks for conflict markers in files
    private static String CONFLICT_MARKER = "(<<<<<<<)([\\s\\S]*?)(>>>>>>>)"  
    // Looks for both english and portuguese system for merge messages
    private static String CONFLICT_INDICATOR = "(CONFLICT)|(CONFLITO)"

    @Override
    public void collectStatistics(Project project, MergeCommit mergeCommit) {
        String outputPath = MiningFramework.getOutputPath()
        File resultsFile = new File("${outputPath}/statistics/results.csv")

        boolean isOctopus = mergeCommit.isOctopus()

        String mergeMessage = replayMergeScenario(project, mergeCommit)
        int numberOfMergeConflicts = getNumberOfMergeConflicts(project, mergeCommit)
        boolean mergeConflictOcurrence = numberOfMergeConflicts > 0
        int numberOfConflictingFiles = getNumberOfConflictingFiles(project, mergeCommit)

        double numberOfDevelopersMean = getNumberOfDevelopersMean(project, mergeCommit)
        double numberOfCommitsMean = getNumberOfCommitsMean(project, mergeCommit)
        double numberOfChangedFilesMean = getNumberOfChangedFilesMean(project, mergeCommit)
        double numberOfChangedLinesMean = getNumberOfChangedLinesMean(project, mergeCommit)
        double durationMean = getDurationMean(project, mergeCommit)
        int conclusionDelay = getConclusionDelay(project, mergeCommit)
        String remoteRepositoryURL = MiningFramework.getResultsRemoteRepositoryURL()
        if(MiningFramework.isPushCommandActive()) {
            File resultsFileLinks = new File("${outputPath}/statistics/results-links.csv")
            String projectLink = addLink(remoteRepositoryURL, project.getName())
            String mergeCommitSHALink = addLink(remoteRepositoryURL, "${project.getName()}/files/${project.getName()}/${mergeCommit.getSHA()}")

            resultsFileLinks << "${projectLink},${mergeCommitSHALink},${isOctopus},${numberOfMergeConflicts},${mergeConflictOcurrence},${numberOfConflictingFiles},${numberOfDevelopersMean},${numberOfCommitsMean},${numberOfChangedFilesMean},${numberOfChangedLinesMean},${durationMean},${conclusionDelay}\n"
        } 
        resultsFile << "${project.getName()},${mergeCommit.getSHA()},${isOctopus},${numberOfMergeConflicts},${mergeConflictOcurrence},${numberOfConflictingFiles},${numberOfDevelopersMean},${numberOfCommitsMean},${numberOfChangedFilesMean},${numberOfChangedLinesMean},${durationMean},${conclusionDelay}\n"


        println "Statistics collection finished!"
    }
    
    private String addLink(String url, String path) {
        return "=HYPERLINK(${url}/tree/master/output-${path};${path})"
    }

    private int getNumberOfMergeConflicts(Project project, MergeCommit mergeCommit) {
        int numberOfMergeConflicts = 0
        Process mergeSimulation = replayMergeScenario(project, mergeCommit)
        mergeSimulation.waitFor()
        
        String mergeFiles = ProcessRunner.runProcess(project.getPath(), 'git', 'diff').getText()
        List<String> list = new ArrayList<String>()
        Pattern pattern = Pattern.compile(CONFLICT_MARKER)
        Matcher m = pattern.matcher(mergeFiles)
       
        while (m.find()) {
            numberOfMergeConflicts++
        }

        Process returnToMaster = returnToMaster(project)
        returnToMaster.waitFor()
    
        return numberOfMergeConflicts
    }

    private int getNumberOfConflictingFiles(Project project, MergeCommit mergeCommit) {
        Set<String> conflictingFiles = new HashSet<String>()

        String mergeOutput = replayMergeScenario(project, mergeCommit).getText()

        def lines = mergeOutput.split("\n")
        Pattern pattern = Pattern.compile(CONFLICT_INDICATOR)

        for (line in lines) {
            Matcher m = pattern.matcher(line)
            if (m.find())
                conflictingFiles.add(line)
        }

        returnToMaster(project)
        return conflictingFiles.size()
    }
    
    private Process replayMergeScenario(Project project, MergeCommit mergeCommit) {
        Process checkoutLeft = ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', mergeCommit.getLeftSHA())
        checkoutLeft.waitFor()

        return ProcessRunner.runProcess(project.getPath(), 'git', 'merge', mergeCommit.getRightSHA())   
    }

    private Process returnToMaster(Project project) {
        Process resetChanges = ProcessRunner.runProcess(project.getPath(), 'git', 'reset', '--hard')
        resetChanges.waitFor()

        return ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', 'master')
    }


    private double getNumberOfDevelopersMean(Project project, MergeCommit mergeCommit) {
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

    private double getNumberOfCommitsMean(Project project, MergeCommit mergeCommit) {
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

    private double getNumberOfChangedFilesMean(Project project, MergeCommit mergeCommit) {
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

    private double getNumberOfChangedLinesMean(Project project, MergeCommit mergeCommit) {
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

    private double getDurationMean(Project project, MergeCommit mergeCommit) {
        String[] parents = mergeCommit.getParentsSHA()
        int[] numberOfDaysPassed = new int[parents.length]
        SimpleDateFormat formatter = new SimpleDateFormat('yyyy-mm-dd')

        for (int i = 0; i < parents.length; i++) {
            Process gitLog = ProcessRunner.runProcess(project.getPath(), 'git', 'log', '--date=short', '--pretty=%H%n%ad', parents[i])

            numberOfDaysPassed[i] = 0
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(gitLog.getInputStream()))
                ArrayList<String> output = reader.readLines()
    
                Date parentDate = formatter.parse(output[1])

                int j;
                for(j = 2; j < output.size(); j++) {
                    if(output[j].equals(mergeCommit.getAncestorSHA()))
                        break
                }

                Date ancestorDate = formatter.parse(output[j + 1])
                long diff = parentDate.getTime() - ancestorDate.getTime()
                numberOfDaysPassed[i] = Math.abs(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS))
            
            } catch(IOException | ParseException e) {
                println e
            }
        }
        return geometricMean(numberOfDaysPassed)
    }

    private int getConclusionDelay(Project project, MergeCommit mergeCommit) {
        if (mergeCommit.isOctopus()) {
            println "Conclusion delay is not supported for octopus merge commits, delay was set to -1"
            return -1;
        }
        String[] parents = mergeCommit.getParentsSHA()
        Date[] commitDates = new Date[parents.length]
        SimpleDateFormat formatter = new SimpleDateFormat('yyyy-mm-dd')

        // This metric implies two parents only.
        for (int i = 0; i < 2; i++) {
            Process gitShow = ProcessRunner.runProcess(project.getPath(), 'git', 'show', '--date=short', '--pretty=%ad', parents[i])

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(gitShow.getInputStream()))
                ArrayList<String> output = reader.readLines()
                commitDates[i] = formatter.parse(output[0])
            } catch(IOException | ParseException e) {
                e.printStackTrace()
            }
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