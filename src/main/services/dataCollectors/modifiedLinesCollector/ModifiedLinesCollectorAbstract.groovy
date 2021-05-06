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

    protected ModifiedStaticBlocksHelper modifiedStaticBlocksHelper;
    protected ModifiedMethodsHelper modifiedMethodsHelper;
    protected RevisionsFilesCollector revisionsCollector = new RevisionsFilesCollector();

    abstract def void collectData(Project project, MergeCommit mergeCommit)

    abstract def void createExperimentalDataFiles(String outputPath)

    protected void collectorData(Tuple2<ModifiedMethod, ModifiedMethod> leftAndRightMethods, ModifiedMethod mergeMethod, Project project, MergeCommit mergeCommit, String className) {
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

    protected void collectorData(Tuple2<ModifiedStaticBlock, ModifiedStaticBlock> leftAndRightStaticBlocks, ModifiedStaticBlock mergeStaticBlock, Project project, MergeCommit mergeCommit, String className) {
        ModifiedStaticBlock leftStaticBlock = leftAndRightStaticBlocks.getV1();
        ModifiedStaticBlock rightStaticBlock = leftAndRightStaticBlocks.getV2();

        Set<Integer> leftAddedLines = new HashSet<Integer>();
        Set<Integer> leftDeletedLines = new HashSet<Integer>();
        Set<Integer> rightAddedLines = new HashSet<Integer>();
        Set<Integer> rightDeletedLines = new HashSet<Integer>();

        // for each modified line in merge
        for (def mergeLine : mergeStaticBlock.getModifiedLines()) {
            // if it is at left's modified lines add it to left list
            if (leftStaticBlock.getModifiedLines().contains(mergeLine)) {
                if (mergeLine.getType() == ModifiedLine.ModificationType.Removed) {
                    leftDeletedLines.add(mergeLine.getNumber());
                } else {
                    leftAddedLines.add(mergeLine.getNumber());
                }
            }
            // if it is at rights's modified lines add it to right list
            if (rightStaticBlock.getModifiedLines().contains(mergeLine)) {
                if (mergeLine.getType() == ModifiedLine.ModificationType.Removed) {
                    rightDeletedLines.add(mergeLine.getNumber());
                } else {
                    rightAddedLines.add(mergeLine.getNumber());
                }
            }
        }

        // prints results to a csv file
        printResults(project, mergeCommit, className, mergeStaticBlock.getIdentifier(), leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines);
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
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())

        return leftModifiedFiles.intersect(rightModifiedFiles)
    }

    protected Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> getMutuallyModifiedMethods(Project project, MergeCommit mergeCommit, String filePath) {
        Set<ModifiedMethod> leftModifiedMethods = modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA())
        Set<ModifiedMethod> rightModifiedMethods = modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA())
        return intersectAndBuildMap(leftModifiedMethods, rightModifiedMethods)
    }

    protected Map<String, Tuple2<ModifiedMethod, ModifiedStaticBlock>> getMutuallyModifiedStaticBlocks(Project project, MergeCommit mergeCommit, String filePath) {
        Set<ModifiedMethod> leftModifiedStaticBlocks = modifiedStaticBlocksHelper.getModifiedStaticBlocks(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA())
        Set<ModifiedMethod> rightModifiedStaticBlocks = modifiedStaticBlocksHelper.getModifiedStaticBlocks(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA())
        return intersectAndBuildMapStaticBlock(leftModifiedStaticBlocks, rightModifiedStaticBlocks )
    }


    protected Map<String, Tuple2<ModifiedStaticBlock, ModifiedStaticBlock>> intersectAndBuildMapStaticBlock(Set<ModifiedStaticBlock> leftModifiedStaticBlocks, Set<ModifiedStaticBlock> rightModifiedStaticBlocks) {
        Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> intersection = [:]

        for(leftStaticBlock in leftModifiedStaticBlocks) {
            for(rightStaticBlock in rightModifiedStaticBlocks) {
                if(leftStaticBlock == rightStaticBlock) {
                    intersection.put(leftStaticBlock.getIdentifier(), new Tuple2(leftStaticBlock, rightStaticBlock))
                }
            }
        }

        return intersection
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
    private synchronized void printResults(Project project, MergeCommit mergeCommit, String className, String modifiedDeclarationSignature,
                                           HashSet<Integer> leftAddedLines, HashSet<Integer> leftDeletedLines, HashSet<Integer> rightAddedLines,
                                           HashSet<Integer> rightDeletedLines) {

        experimentalDataFile << "${project.getName()};${mergeCommit.getSHA()};${className};${modifiedDeclarationSignature};${leftAddedLines};${leftDeletedLines};${rightAddedLines};${rightDeletedLines}\n"

        // Add links.
        if(arguments.isPushCommandActive())
            addLinks(project.getName(), mergeCommit.getSHA(), className, modifiedDeclarationSignature, leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines, arguments.getResultsRemoteRepositoryURL())

    }
}