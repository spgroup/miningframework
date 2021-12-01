package services.commitFilters

import interfaces.CommitFilter
import project.*
import util.*
import services.dataCollectors.modifiedLinesCollector.ModifiedStaticBlocksHelper

import java.util.stream.Collectors

import static app.MiningFramework.arguments

class MutuallyModifiedStaticBlocksCommitFilter implements CommitFilter {

    private modifiedStaticBlocksHelper = new ModifiedStaticBlocksHelper("diffj.jar");

    boolean applyFilter(Project project, MergeCommit mergeCommit) {
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
        Set<String> mutuallyModifiedFiles = new HashSet<String>(leftModifiedFiles)
        obtainResultsForProject(project,mergeCommit,leftModifiedFiles, "files_all");
        mutuallyModifiedFiles.retainAll(rightModifiedFiles)
        obtainResultsForProject(project,mergeCommit,mutuallyModifiedFiles, "mutuallyModifiedFiles");
        for(file in mutuallyModifiedFiles) {
           //if (file.contains("Jenkins")){
                    //|| file.contains("ComputerSet") || file.contains("ProcessTree") || file.contains("StreamTaskListener")) {

            Set<String> leftModifiedContextStaticBlocks = getModifiedContextStaticBlocks(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit)
            Set<String> rightModifiedModifiedContextStaticBlocks = getModifiedContextStaticBlocks(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA(), mergeCommit)

           // leftModifiedContextStaticBlocks.retainAll(rightModifiedModifiedContextStaticBlocks) // Intersection.

            if(leftModifiedContextStaticBlocks.size() > 0 || rightModifiedModifiedContextStaticBlocks.size() >0 )
                return true
            //}
        }

        return true
    }

    private Set<String> getModifiedContextStaticBlocks(Project project, String filePath, String ancestorSHA, String targetSHA, MergeCommit mergeCommit) {
        return modifiedStaticBlocksHelper.getModifiedStaticBlocks(project, filePath, ancestorSHA, targetSHA, mergeCommit)
                .stream()
                .map(identifier -> identifier.getIdentifier())
                .collect(Collectors.toSet())
    }
    private void obtainResultsForProject(Project project , MergeCommit mergeCommit,Set<String> leftModifiedFiles, String name) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        File obtainResultsForProjects = new File(dataFolder.getAbsolutePath() + "/"+ name + "_" + project.getName() + ".csv")
        if (!obtainResultsForProjects.exists()) {
            obtainResultsForProjects << 'Merge commit; Ancestor; Parent 1; Parent 2; files\n'
        }
        for(String path : leftModifiedFiles)
           obtainResultsForProjects  << "${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};${path};\n"
    }
}