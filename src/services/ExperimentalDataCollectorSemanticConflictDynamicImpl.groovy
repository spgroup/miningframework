package services

@Grab(group='org.apache.commons', module='commons-lang3', version='3.9')
import main.interfaces.DataCollector
import main.project.MergeCommit
import main.project.Project
import main.util.FileManager
import main.util.ProcessRunner
import main.util.MergeHelper
import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern
import static groovy.io.FileType.FILES

import static main.app.MiningFramework.arguments

class ExperimentalDataCollectorSemanticConflictDynamicImpl extends ExperimentalDataCollectorImpl {

    @Override
    protected String addHeaderLinesForOutputFile(){
        return 'project;merge commit;left commit;right commit;base commit;className;method;local_clone;local_clone_module_analysis;empty_diff_base_left;empty_diff_base_right;empty_diff_base_merge\n'
    }
    
    @Override
    protected String addMergeCommitInfoIntoOutputFile(Project project, MergeCommit mergeCommit, String className, String modifiedDeclarationSignature,
                      HashSet<Integer> leftAddedLines, HashSet<Tuple2> leftDeletedLines, HashSet<Integer> rightAddedLines,
                      HashSet<Tuple2> rightDeletedLines){
        ArrayList<Boolean> emptyDiffsByParents = MergeHelper.checkForEmptyDiffByParents(project, mergeCommit, className)
        return "${project.getName()};${mergeCommit.getSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};${mergeCommit.getAncestorSHA()};${className};\"${modifiedDeclarationSignature.replace(",","|")}\";${project.getFullLocalPath()};${project.getFullLocalPath()};${emptyDiffsByParents[0]};${emptyDiffsByParents[1]};${emptyDiffsByParents[2]}\n"
    }

}