package services.commitFilters

import interfaces.CommitFilter
import project.*
import util.*
import services.dataCollectors.modifiedLinesCollector.ModifiedStaticBlocksHelper

import java.util.stream.Collectors

import static app.MiningFramework.arguments

class MutuallyModifiedStaticBlocksCommitFilter implements CommitFilter {

    private modifiedStaticBlocksHelper = new ModifiedStaticBlocksHelper("diffj.jar");
    private File filteredScenariosIniatilizationBlock = null;

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

        Set<String> mutuallyModifiedFiles = null;
       if(leftModifiedFiles.size() > 0){
           mutuallyModifiedFiles  = new HashSet<String>(leftModifiedFiles)
           mutuallyModifiedFiles.retainAll(rightModifiedFiles)
           obtainResultsForProject(project,mergeCommit,leftModifiedFiles, "files_all");
       }else{
           mutuallyModifiedFiles  = new HashSet<String>(rightModifiedFiles)
           mutuallyModifiedFiles.retainAll(leftModifiedFiles)
           obtainResultsForProject(project,mergeCommit,leftModifiedFiles, "files_all");
       }
        obtainResultsForProject(project, mergeCommit, mutuallyModifiedFiles, "mutuallyModifiedFiles");

        for(file in mutuallyModifiedFiles) {
        //   if (file.contains("JenkinsRule")){
                    //|| file.contains("ComputerSet") || file.contains("ProcessTree") || file.contains("StreamTaskListener")) {

            Set<String> leftModifiedContextStaticBlocks = getModifiedContextStaticBlocks(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit)
            Set<String> rightModifiedModifiedContextStaticBlocks = getModifiedContextStaticBlocks(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA(), mergeCommit)

           // leftModifiedContextStaticBlocks.retainAll(rightModifiedModifiedContextStaticBlocks) // Intersection.

            if(leftModifiedContextStaticBlocks.size() > 0 || rightModifiedModifiedContextStaticBlocks.size() >0 ){
                createDataFilesExperimentalStaticBlock(project,mergeCommit,file, leftModifiedContextStaticBlocks.size() + rightModifiedModifiedContextStaticBlocks.size() )

                return true
             }
        }
       // }

        return false
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
    private void createDataFilesExperimentalStaticBlock(Project project,MergeCommit mergeCommit, String targetFile, int qtdStaticBlock) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        filteredScenariosIniatilizationBlock = new File(dataFolder.getAbsolutePath() + "/results-IniatilizationQTD.csv")
        if (!filteredScenariosIniatilizationBlock.exists()) {
            filteredScenariosIniatilizationBlock << 'project; merge commit ;ancestorSHA; left; right; hasIniatializationBlock;  qtd_static\n'
        }

        filteredScenariosIniatilizationBlock << "${project.getName()};${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};${targetFile};${qtdStaticBlock}\n"
    }
}