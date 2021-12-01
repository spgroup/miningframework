package services.dataCollectors.modifiedLinesCollector


import project.MergeCommit
import project.Project
import util.TypeNameHelper

import static app.MiningFramework.arguments

/**
 * @requires: that a diffj cli is in the dependencies folder and that diff (textual diff tool) is installed
 * @provides: a [outputPath]/data/results.csv file with the following format:
 * project;merge commit;className;staticBlock;left modifications;left deletions;right modifications;right deletions
 */
class ModifiedLinesStaticBlockCollector extends ModifiedLinesCollectorAbstract {

    public ModifiedLinesStaticBlockCollector() {
        modifiedStaticBlocksHelper = new ModifiedStaticBlocksHelper("diffj.jar");
    }

    void collectData(Project project, MergeCommit mergeCommit) {
        createOutputFiles(arguments.getOutputPath())
        Set<String> mutuallyModifiedFiles = getFilesModifiedByBothParents(project, mergeCommit);

        for (String filePath : mutuallyModifiedFiles) {
            // get merge revision modified Static Blocks
            Set<ModifiedStaticBlock> allModifiedStaticBlocks = modifiedStaticBlocksHelper.getModifiedStaticBlocks(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getSHA(),mergeCommit)
            // get staticBlock modified by both left and right revisions
            Map<String, Tuple2<ModifiedStaticBlock, ModifiedStaticBlock>> mutuallyModifiedStaticBlocks = getMutuallyModifiedStaticBlocks(project, mergeCommit, filePath);

            boolean fileHasMutuallyModifiedStaticBlocks = !mutuallyModifiedStaticBlocks.isEmpty()
            if (fileHasMutuallyModifiedStaticBlocks) {
                // get file class name
                String className = TypeNameHelper.getFullyQualifiedName(project, filePath, mergeCommit.getAncestorSHA())

                // calling a data collector here because in this specific case we only need
                // revisions for the cases where there are mutually modified staticBlock in this class
                revisionsCollector.collectDataFromFile(project, mergeCommit, filePath);

                for (def staticBlock : allModifiedStaticBlocks) {
                    // get left and right staticBlock for the specific merge staticBlock
                    Tuple2<ModifiedStaticBlock, ModifiedStaticBlock> leftAndRightStaticBlocks = mutuallyModifiedStaticBlocks[staticBlock.getIdentifier()];
                    // if its null than this static block wasn't modified by both left and right

                    boolean staticBlockWasModifiedByBothParents = leftAndRightStaticBlocks != null
                    // we loop in all static Block and discard the cases that were not modified by both left and right
                    // instead of looping directly the mutually modified static Block because its cheaper to do it like this
                    // because the other way we would have to search the all static Block list for each iteration to get merge
                    // revision static Block
                    if (staticBlockWasModifiedByBothParents) {
                        collectorData(leftAndRightStaticBlocks, staticBlock, project, mergeCommit, className)
                    }

                }

            }


        }
        println "${project.getName()} - ModifiedLinesCollector collection finished"
    }

    void createExperimentalDataFiles(String outputPath) {
        this.experimentalDataFile = new File(outputPath + "/data/results.csv")
        if (!experimentalDataFile.exists()) {
            this.experimentalDataFile << 'project;merge commit;className;staticBlock;left modifications;left deletions;right modifications;right deletions\n'
        }

        if (arguments.isPushCommandActive()) {
            this.experimentalDataFileWithLinks = new File("${outputPath}/data/result-links.csv");
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

}