package services.commitFilters

import interfaces.CommitFilter
import project.*
import services.dataCollectors.staticBlockCollector.SpreadsheetBuilder
import util.*
import services.dataCollectors.staticBlockCollector.StaticBlocksHelper
import java.util.stream.Collectors


class MutuallyModifiedStaticBlocksCommitFilter implements CommitFilter {

    private modifiedStaticBlocksHelper = new StaticBlocksHelper("diffj.jar");

    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        SpreadsheetBuilder.obtainResultsSpreadsheetForProject(project, mergeCommit, SpreadsheetBuilder.FILTER_MERGES_SCENARIOS);
        return containsMutuallyModifiedStaticBlocks(project, mergeCommit)
    }

    private boolean containsMutuallyModifiedStaticBlocks(Project project, MergeCommit mergeCommit) {
        if (mergeCommit.getAncestorSHA() == null) {
            /**
             * Some merge scenarios don't return an valid ancestor SHA this check prevents
             * unexpected crashes
             */
            return false;
        }

        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())
        Set<String> mutuallyModifiedNamesFiles = new HashSet<String>(leftModifiedFiles)
        mutuallyModifiedNamesFiles.retainAll(rightModifiedFiles)

         // Step 2 of filter: Both branches changed at least one common file
        if(mutuallyModifiedNamesFiles.size() > 0) {
            SpreadsheetBuilder.obtainResultsSpreadsheetForProject(project, mergeCommit, SpreadsheetBuilder.FILTER_BRANCHES_CHANGED_LEAST_ONE_COMMON_FILE);
        }
        for(file in mutuallyModifiedNamesFiles) {
                Set<String> leftModifiedContextStaticBlocks = getModifiedContextStaticBlocks(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit)
                Set<String> rightModifiedContextStaticBlocks = getModifiedContextStaticBlocks(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA(), mergeCommit)

                //Step 4 of filter: both branches changed at least one initialization block
                if (leftModifiedContextStaticBlocks.size() > 0 && rightModifiedContextStaticBlocks.size() > 0) {
                    SpreadsheetBuilder.obtainResultsSpreadsheetForProject(project, mergeCommit, file, leftModifiedContextStaticBlocks.size() + rightModifiedContextStaticBlocks.size())
                    return true
                }
      }
        return false
    }
    public List<String> getModifiedJavaFilePaths(Project project, MergeCommit mergeCommit) {
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())
        Set<String> mutuallyModifiedNamesFiles = new HashSet<String>(leftModifiedFiles)
        mutuallyModifiedNamesFiles.retainAll(rightModifiedFiles)

        List<String> modifiedContextStaticBlocks = new ArrayList<String>();
        for(file in mutuallyModifiedNamesFiles) {
                Set<String> leftModifiedContextStaticBlocks = getModifiedContextStaticBlocksFiles(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit)
                Set<String> rightModifiedContextStaticBlocks = getModifiedContextStaticBlocksFiles(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA(), mergeCommit)

                if (leftModifiedContextStaticBlocks.size() > 0 && rightModifiedContextStaticBlocks.size() > 0) {
                    modifiedContextStaticBlocks.addAll(leftModifiedContextStaticBlocks)
                }
        }
        return modifiedContextStaticBlocks;
    }
    public Set<String> getModifiedContextStaticBlocksFiles(Project project, String filePath, String ancestorSHA, String targetSHA, MergeCommit mergeCommit) {
        return modifiedStaticBlocksHelper.getModifiedStaticBlocks(project, filePath, ancestorSHA, targetSHA, mergeCommit)
                .stream()
                .map(path -> path.getPath())
                .collect(Collectors.toSet())
    }
    private Set<String> getModifiedContextStaticBlocks(Project project, String filePath, String ancestorSHA, String targetSHA, MergeCommit mergeCommit) {
        return modifiedStaticBlocksHelper.getModifiedStaticBlocks(project, filePath, ancestorSHA, targetSHA, mergeCommit)
                .stream()
                .map(identifier -> identifier.getIdentifier())
                .collect(Collectors.toSet())
    }
}