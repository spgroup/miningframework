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
 * @provides: a [outputPath]/data/results.csv file with the following format:
 * project;merge commit;className;method;left modifications;left deletions;right modifications;right deletions
 */
abstract class ModifiedLinesCollectorAbstract implements DataCollector {

    protected File experimentalDataFile;
    protected File experimentalDataFileWithLinks;

    protected ModifiedMethodsHelper modifiedMethodsHelper;
    protected RevisionsFilesCollector revisionsCollector = new RevisionsFilesCollector();

    abstract def void collectData(Project project, MergeCommit mergeCommit)

    abstract def void createExperimentalDataFiles(String outputPath)

    protected void collectMethodData(Tuple2<ModifiedMethod, ModifiedMethod> leftAndRightMethods, ModifiedMethod mergeMethod, Project project, MergeCommit mergeCommit, String className) {
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

    protected void collectFileData(Set<ModifiedLine> leftModifiedLines, Set<ModifiedLine> rightModifiedLines, Project project, MergeCommit mergeCommit, String filePath) {
        Set<Integer> leftAddedLines = new HashSet<Integer>();
        Set<Integer> leftDeletedLines = new HashSet<Integer>();
        Set<Integer> leftChangedLines = new HashSet<Integer>();
        Set<Integer> rightAddedLines = new HashSet<Integer>();
        Set<Integer> rightDeletedLines = new HashSet<Integer>();
        Set<Integer> rightChangedLines = new HashSet<Integer>();

        for (def line : leftModifiedLines) {
            if (line.getType() == ModifiedLine.ModificationType.Removed) {
                leftDeletedLines.add(line.getNumber());
            } else if (line.getType() == ModifiedLine.ModificationType.Added){
                leftAddedLines.add(line.getNumber());
            } else {
                leftChangedLines.add(line.getNumber())
            }
        }

        for (def line : rightModifiedLines) {
            if (line.getType() == ModifiedLine.ModificationType.Removed) {
                rightDeletedLines.add(line.getNumber());
            } else if (line.getType() == ModifiedLine.ModificationType.Added){
                rightAddedLines.add(line.getNumber());
            } else {
                rightChangedLines.add(line.getNumber())
            }
        }

        // prints results to a csv file
        printResults(project, mergeCommit, filePath, leftAddedLines, leftDeletedLines, leftChangedLines, rightAddedLines, rightDeletedLines, rightChangedLines);
    }

    protected void createOutputFiles(String outputPath) {
        createExperimentalDataDir(outputPath)
        createExperimentalDataFiles(outputPath)
    }

    protected void createExperimentalDataDir(String outputPath) {
        File experimentalDataDir = new File(outputPath + '/data')

        if (!experimentalDataDir.exists()) {
            experimentalDataDir.mkdirs()
        }
    }

    protected Set<String> getFilesModifiedByBothParents(Project project, MergeCommit mergeCommit) {
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA(), arguments.getFileExtension())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA(), arguments.getFileExtension())

        return leftModifiedFiles.intersect(rightModifiedFiles)
    }

    protected Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> getMutuallyModifiedMethods(Project project, MergeCommit mergeCommit, String filePath) {
        Set<ModifiedMethod> leftModifiedMethods = modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA())
        Set<ModifiedMethod> rightModifiedMethods = modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA())
        return intersectAndBuildMap(leftModifiedMethods, rightModifiedMethods)
    }

    protected Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> intersectAndBuildMap(Set<ModifiedMethod> leftModifiedMethods, Set<ModifiedMethod> rightModifiedMethods) {
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