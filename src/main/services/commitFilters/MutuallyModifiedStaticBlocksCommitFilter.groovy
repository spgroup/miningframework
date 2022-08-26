package services.commitFilters

import interfaces.CommitFilter
import project.*
import util.*
import services.dataCollectors.staticBlockCollector.StaticBlocksHelper

import java.util.stream.Collectors

import static app.MiningFramework.arguments

class MutuallyModifiedStaticBlocksCommitFilter implements CommitFilter {

    private modifiedStaticBlocksHelper = new StaticBlocksHelper("diffj.jar");
    private File filteredScenariosIniatilizationBlock = null;

    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        obtainResultsForProject(project,mergeCommit)
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
        mutuallyModifiedFiles.retainAll(rightModifiedFiles)

        if(mutuallyModifiedFiles.size() > 0)
          obtainResultsForProject(project, mergeCommit, mutuallyModifiedFiles, "2_results_branches_changed_least_one_common_file");

        for(file in mutuallyModifiedFiles) {
          if(file.containsIgnoreCase("RealmSourceCodeGenerator")) {
              Set<String> leftModifiedContextStaticBlocks = getModifiedContextStaticBlocks(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit)
              Set<String> rightModifiedContextStaticBlocks = getModifiedContextStaticBlocks(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA(), mergeCommit)

              /*
            * Step 4 of filter: both branches changed at least one initialization block
             */
              if (leftModifiedContextStaticBlocks.size() > 0 && rightModifiedContextStaticBlocks.size() > 0) {
                  createDataFilesExperimentalStaticBlock(project, mergeCommit, file, leftModifiedContextStaticBlocks.size() + rightModifiedContextStaticBlocks.size())

                  return true
              }
          }
      }

        return false
    }
    public List<String> getModifiedJavaFilePaths(Project project, MergeCommit mergeCommit) {
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())
        Set<String> mutuallyModifiedFiles = new HashSet<String>(leftModifiedFiles)
        mutuallyModifiedFiles.retainAll(rightModifiedFiles)

        List<String> modifiedContextStaticBlocks = new ArrayList<String>();
        for(file in mutuallyModifiedFiles) {
            Set<String> leftModifiedContextStaticBlocks = getModifiedContextStaticBlocksFiles(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit)
            Set<String> rightModifiedContextStaticBlocks = getModifiedContextStaticBlocksFiles(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA(), mergeCommit)

            if (leftModifiedContextStaticBlocks.size() > 0 && rightModifiedContextStaticBlocks.size() > 0) {
                modifiedContextStaticBlocks.addAll(leftModifiedContextStaticBlocks)
            }
              /*  modifiedContextStaticBlocks.addAll(getModifiedContextStaticBlocksFiles(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit))
                if(!(modifiedContextStaticBlocks.size() > 0)) {
                    modifiedContextStaticBlocks.addAll(getModifiedContextStaticBlocksFiles(project, file, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA(), mergeCommit))
                }*/
        }

        return modifiedContextStaticBlocks;

    }
    public Set<String> getModifiedContextStaticBlocksFiles(Project project, String filePath, String ancestorSHA, String targetSHA, MergeCommit mergeCommit) {
        print"${filePath},${ancestorSHA}"
        return modifiedStaticBlocksHelper.getModifiedStaticBlocks(project, filePath, ancestorSHA, targetSHA, mergeCommit)
                .stream()
                .map(path -> path.getPath())
                .collect(Collectors.toList())
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
            obtainResultsForProjects << 'Merge commit; Ancestor; Parent 1; Parent 2;\n'
        }

           obtainResultsForProjects  << "${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};\n"
    }
    private void createDataFilesExperimentalStaticBlock(Project project,MergeCommit mergeCommit, String targetFile, int qtdStaticBlock) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        filteredScenariosIniatilizationBlock = new File(dataFolder.getAbsolutePath() + "/4_results_branched_changed_least_on_iniatilizationBlock" + "_" + project.getName() + ".csv")
        if (!filteredScenariosIniatilizationBlock.exists()) {
            filteredScenariosIniatilizationBlock << 'project; merge commit ;ancestorSHA; left; right; hasIniatializationBlock;  qtd_static\n'
        }

        filteredScenariosIniatilizationBlock << "${project.getName()};${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};${targetFile};${qtdStaticBlock}\n"
    }
    private void obtainResultsForProject(Project project , MergeCommit mergeCommit) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        File obtainResultsForProjects = new File(dataFolder.getAbsolutePath() + "/1_results_merges_scenarios_"+project.getName()+".csv")
        if (!obtainResultsForProjects.exists()) {
            obtainResultsForProjects << 'Merge commit, Parent 1, Parent 2\n'
        }
        obtainResultsForProjects << "${mergeCommit.getSHA()},${mergeCommit.getLeftSHA()},${mergeCommit.getRightSHA()}\n"
    }
}
