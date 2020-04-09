package services.dataCollectors.modifiedLinesCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import util.FileManager
import services.dataCollectors.RevisionsFilesCollector
import util.TypeNameHelper

import static app.MiningFramework.arguments


/**
 * @requires: that a diffj cli is in the dependencies folder and that diff (textual diff tool) is installed
 * @produces: a [outputPath]/data/results.csv file with the following format:
 * project;merge commit;className;method;left modifications;left deletions;right modifications;right deletions
 */
class ModifiedLinesCollector implements DataCollector {

    private File experimentalDataFile;
    private File experimentalDataFileWithLinks;

    private ModifiedMethodsHelper modifiedMethodsHelper = new ModifiedMethodsHelper();
    private RevisionsFilesCollector revisionsCollector = new RevisionsFilesCollector();

    void collectData(Project project, MergeCommit mergeCommit) {
        createOutputFiles(arguments.getOutputPath())
        Set<String> mutuallyModifiedFiles = getFilesModifiedByBothParents(project, mergeCommit);

        for (String filePath : mutuallyModifiedFiles) {
            // get merge revision modified methods
            Set<ModifiedMethod> allModifiedMethods = modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getSHA())
            // get methods modified by both left and right revisions
            Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> mutuallyModifiedMethods = getMutuallyModifiedMethods(project, mergeCommit, filePath);

            boolean fileHasMutuallyModifiedMethods = !mutuallyModifiedMethods.isEmpty()
            if (fileHasMutuallyModifiedMethods) {
                // get file class name
                String className = TypeNameHelper.getFullyQualifiedName(project, filePath, mergeCommit.getAncestorSHA())

                // calling a data collector here because in this specific case we only need
                // revisions for the cases where there are mutually modified methods in this class
                revisionsCollector.collectDataFromFile(project, mergeCommit, filePath);

                for (def method : allModifiedMethods) {
                    // get left and right methods for the specific merge method
                    Tuple2<ModifiedMethod, ModifiedMethod> leftAndRightMethods = mutuallyModifiedMethods[method.getSignature()];
                    // if its null than this methods wasn't modified by both left and right

                    boolean methodWasModifiedByBothParents = leftAndRightMethods != null
                    // we loop in all methods and discard the cases that were not modified by both left and right
                    // instead of looping directly the mutually modified methods because its cheaper to do it like this
                    // because the other way we would have to search the all methods list for each iteration to get merge
                    // revision method
                    if (methodWasModifiedByBothParents) {
                        collectMethodData(leftAndRightMethods, method, project, mergeCommit, className)
                    }

                }

            }


        }
        println "${project.getName()} - ModifiedLinesCollector collection finished"
    }

    private void collectMethodData(Tuple2<ModifiedMethod, ModifiedMethod> leftAndRightMethods, ModifiedMethod mergeMethod, Project project, MergeCommit mergeCommit, String className) {
        ModifiedMethod leftMethod = leftAndRightMethods.getV1();
        ModifiedMethod rightMethod = leftAndRightMethods.getV2();

        Set<Integer> leftAddedLines = new HashSet<Integer>();
        Set<Integer> leftDeletedLines = new HashSet<Integer>();
        Set<Integer> rightAddedLines = new HashSet<Integer>();
        Set<Integer> rightDeletedLines = new HashSet<Integer>();

        // for each modified line in merge
        for (def mergeLine : mergeMethod.getModifiedLines()) {
            // if it is at left's modified lines add it to left list
            if (leftMethod.getModifiedLines().contains(mergeLine)) {
                if (mergeLine.getType() == ModifiedLine.ModificationType.Removed) {
                    leftDeletedLines.add(mergeLine.getNumber());
                } else {
                    leftAddedLines.add(mergeLine.getNumber());
                }
            }
            // if it is at rights's modified lines add it to right list
            if (rightMethod.getModifiedLines().contains(mergeLine)) {
                if (mergeLine.getType() == ModifiedLine.ModificationType.Removed) {
                    rightDeletedLines.add(mergeLine.getNumber());
                } else {
                    rightAddedLines.add(mergeLine.getNumber());
                }
            }
        }

        // prints results to a csv file
        printResults(project, mergeCommit, className, mergeMethod.getSignature(), leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines);
    }

    private void createOutputFiles(String outputPath) {
        createExperimentalDataDir(outputPath)
        createExperimentalDataFiles(outputPath)
    }

    private void createExperimentalDataFiles(String outputPath) {
        this.experimentalDataFile = new File(outputPath + "/data/results.csv")
        if (!experimentalDataFile.exists()) {
            this.experimentalDataFile << 'project;merge commit;className;method;left modifications;left deletions;right modifications;right deletions\n'
        }

        if (arguments.isPushCommandActive()) {
            this.experimentalDataFileWithLinks = new File("${outputPath}/data/result-links.csv");
        }
    }

    private void createExperimentalDataDir(String outputPath) {
        File experimentalDataDir = new File(outputPath + '/data')

        if (!experimentalDataDir.exists()) {
            experimentalDataDir.mkdirs()
        }
    }

    private synchronized void printResults(Project project, MergeCommit mergeCommit, String className, String modifiedDeclarationSignature,
                      HashSet<Integer> leftAddedLines, HashSet<Integer> leftDeletedLines, HashSet<Integer> rightAddedLines,
                      HashSet<Integer> rightDeletedLines) {

        experimentalDataFile << "${project.getName()};${mergeCommit.getSHA()};${className};${modifiedDeclarationSignature};${leftAddedLines};${leftDeletedLines};${rightAddedLines};${rightDeletedLines}\n"

        // Add links.
        if(arguments.isPushCommandActive())
            addLinks(project.getName(), mergeCommit.getSHA(), className, modifiedDeclarationSignature, leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines, arguments.getResultsRemoteRepositoryURL())

    }

    private Set<String> getFilesModifiedByBothParents(Project project, MergeCommit mergeCommit) {
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())

        return leftModifiedFiles.intersect(rightModifiedFiles)
    }

    private Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> getMutuallyModifiedMethods(Project project, MergeCommit mergeCommit, String filePath) {
        Set<ModifiedMethod> leftModifiedMethods = modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA())
        Set<ModifiedMethod> rightModifiedMethods = modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA())
        return intersectAndBuildMap(leftModifiedMethods, rightModifiedMethods)
    }

    Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> intersectAndBuildMap(Set<ModifiedMethod> leftModifiedMethods, Set<ModifiedMethod> rightModifiedMethods) {
        Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> intersection = [:]

        for(leftMethod in leftModifiedMethods) {
            for(rightMethod in rightModifiedMethods) {
                if(leftMethod == rightMethod) {
                    intersection.put(leftMethod.getSignature(), new Tuple2(leftMethod, rightMethod))
                }
            }
        }

        return intersection
    }    
}