package services.dataCollectors.modifiedLinesCollector

import project.MergeCommit
import project.Project
import util.TypeNameHelper
import static app.MiningFramework.arguments

/**
 * Same collection logic as {@link ModifiedLinesCollector}, but WITHOUT the "mutually modified
 * method" restriction. It reports every method modified by the left OR the right parent (union),
 * instead of only the methods modified by both (intersection). Line numbers are taken from the
 * merge revision (matched by content), exactly like the method-based collector, so the output is
 * consistent with it for scenarios where both developers touched the same method.
 */
class UnrestrictedModifiedLinesCollector extends ModifiedLinesCollectorAbstract {

    public UnrestrictedModifiedLinesCollector() {
        this("dependencies");
    }

    public UnrestrictedModifiedLinesCollector(String dependenciesPath) {
        modifiedMethodsHelper = new ModifiedMethodsHelper("diffj.jar", dependenciesPath);
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        createOutputFiles(arguments.getOutputPath())
        // union of the files modified by either parent (no "modified by both" restriction)
        Set<String> modifiedFiles = getAllModifiedFiles(project, mergeCommit);

        for (String filePath : modifiedFiles) {
            // methods modified in the merge revision (relative to the ancestor); this gives the
            // merge-revision line numbers used in the output
            Set<ModifiedMethod> mergeModifiedMethods = getModifiedMethodsSafe(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getSHA())
            if (mergeModifiedMethods.isEmpty()) {
                continue
            }

            // methods modified by each parent (relative to the ancestor), indexed by signature
            Map<String, ModifiedMethod> leftMethods = mapBySignature(getModifiedMethodsSafe(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA()))
            Map<String, ModifiedMethod> rightMethods = mapBySignature(getModifiedMethodsSafe(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA()))

            String className = TypeNameHelper.getFullyQualifiedName(project, filePath, mergeCommit.getAncestorSHA())
            boolean revisionsCollected = false

            for (ModifiedMethod mergeMethod : mergeModifiedMethods) {
                ModifiedMethod leftMethod = leftMethods.get(mergeMethod.getSignature())
                ModifiedMethod rightMethod = rightMethods.get(mergeMethod.getSignature())

                // union: keep the method if it was modified by the left OR the right parent
                if (leftMethod != null || rightMethod != null) {
                    if (!revisionsCollected) {
                        revisionsCollector.collectDataFromFile(project, mergeCommit, filePath)
                        revisionsCollected = true
                    }
                    collectMethodData(new Tuple2<ModifiedMethod, ModifiedMethod>(leftMethod, rightMethod), mergeMethod, project, mergeCommit, className)
                }
            }
        }
        println "${project.getName()} - UnrestrictedModifiedLinesCollector collection finished"
    }

    private Set<ModifiedMethod> getModifiedMethodsSafe(Project project, String filePath, String ancestorSHA, String targetSHA) {
        try {
            return modifiedMethodsHelper.getModifiedMethods(project, filePath, ancestorSHA, targetSHA)
        } catch (Exception e) {
            // the file might not exist in the ancestor or in the target revision
            return new HashSet<ModifiedMethod>()
        }
    }

    private Map<String, ModifiedMethod> mapBySignature(Set<ModifiedMethod> methods) {
        Map<String, ModifiedMethod> result = [:]
        for (ModifiedMethod method : methods) {
            result.put(method.getSignature(), method)
        }
        return result
    }

    @Override
    void createExperimentalDataFiles(String outputPath) {
        this.experimentalDataFile = new File(outputPath + "/data/results.csv")
        if (!experimentalDataFile.exists()) {
            this.experimentalDataFile << 'project;merge commit;className;method;left modifications;left deletions;right modifications;right deletions\n'
        }

        if (arguments.isPushCommandActive()) {
            this.experimentalDataFileWithLinks = new File("${outputPath}/data/result-links.csv");
            if (!experimentalDataFileWithLinks.exists()) {
                this.experimentalDataFileWithLinks << 'project;merge commit;className;method;left modifications;left deletions;right modifications;right deletions;link\n'
            }
        }
    }

    private synchronized void printResults(Project project, MergeCommit mergeCommit, String className, String modifiedDeclarationSignature,
                                           HashSet<Integer> leftAddedLines, HashSet<Integer> leftDeletedLines,
                                           HashSet<Integer> rightAddedLines, HashSet<Integer> rightDeletedLines) {

        experimentalDataFile << "${project.getName()};${mergeCommit.getSHA()};${className};${modifiedDeclarationSignature};${leftAddedLines};${leftDeletedLines};${rightAddedLines};${rightDeletedLines}\n"

        if (arguments.isPushCommandActive()) {
            String remoteURL = arguments.getResultsRemoteRepositoryURL()
            String link = "${remoteURL}/commit/${mergeCommit.getSHA()}#diff-something" // Simplification
            experimentalDataFileWithLinks << "${project.getName()};${mergeCommit.getSHA()};${className};${modifiedDeclarationSignature};${leftAddedLines};${leftDeletedLines};${rightAddedLines};${rightDeletedLines};${link}\n"
        }
    }

}
