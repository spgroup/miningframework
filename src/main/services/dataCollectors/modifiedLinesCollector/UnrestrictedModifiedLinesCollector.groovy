package services.dataCollectors.modifiedLinesCollector

import project.MergeCommit
import project.Project
import services.dataCollectors.modifiedLinesCollector.exporters.CsvModifiedLinesExporter
import services.dataCollectors.modifiedLinesCollector.exporters.JsonModifiedLinesExporter
import services.dataCollectors.modifiedLinesCollector.exporters.ModifiedLinesExporter
import util.ProcessRunner
import util.TypeNameHelper

import java.util.concurrent.ConcurrentHashMap

import static app.MiningFramework.arguments

/**
 * Same collection logic as {@link ModifiedLinesCollector}, but WITHOUT the "mutually modified
 * method" restriction. It reports every method modified by the left OR the right parent (union),
 * instead of only the methods modified by both (intersection). Line numbers are taken from the
 * merge revision (matched by content), exactly like the method-based collector, so the output is
 * consistent with it for scenarios where both developers touched the same method.
 *
 */
class UnrestrictedModifiedLinesCollector extends ModifiedLinesCollectorAbstract {

    private List<ModifiedLinesExporter> exporters
    private final Map<String, List<MergeMethodModifications>> methodsOfEachMergeCommit = new ConcurrentHashMap<>()

    public UnrestrictedModifiedLinesCollector() {
        this("dependencies");
    }

    public UnrestrictedModifiedLinesCollector(String dependenciesPath) {
        modifiedMethodsHelper = new ModifiedMethodsHelper("diffj.jar", dependenciesPath);
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        createOutputFiles(arguments.getOutputPath())
        methodsOfEachMergeCommit.put(mergeCommit.getSHA(), [])
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
            boolean fileCopied = false

            for (ModifiedMethod mergeMethod : mergeModifiedMethods) {
                ModifiedMethod leftMethod = leftMethods.get(mergeMethod.getSignature())
                ModifiedMethod rightMethod = rightMethods.get(mergeMethod.getSignature())

                // union: keep the method if it was modified by the left OR the right parent
                if (leftMethod != null || rightMethod != null) {
                    if (!fileCopied) {
                        copyFileOfEachRevision(project, mergeCommit, filePath)
                        fileCopied = true
                    }
                    collectMethodData(new Tuple2<ModifiedMethod, ModifiedMethod>(leftMethod, rightMethod), mergeMethod, project, mergeCommit, className)
                }
            }
        }
        exportMethods(project, mergeCommit)
        println "${project.getName()} - UnrestrictedModifiedLinesCollector collection finished"
    }

    // as the synthetic merges dataset: [outputPath]/files/[project]/[merge commit]/source/{base,left,right,merge}/[file
    // path]; a revision without the file (e.g. a parent that deleted it) has no copy
    private void copyFileOfEachRevision(Project project, MergeCommit mergeCommit, String filePath) {
        Map<String, String> revisions = [base : mergeCommit.getAncestorSHA(), left: mergeCommit.getLeftSHA(),
                                         right: mergeCommit.getRightSHA(), merge: mergeCommit.getSHA()]
        File sourceFolder = new File(arguments.getOutputPath(), "files/${project.getName()}/${mergeCommit.getSHA()}/source")

        revisions.each { String revision, String sha ->
            Process gitShow = ProcessRunner.runProcess(project.getPath(), 'git', 'show', "${sha}:${filePath}")
            byte[] content = gitShow.inputStream.bytes
            if (gitShow.waitFor() == 0) {
                File target = new File(sourceFolder, "${revision}/${filePath}")
                target.parentFile.mkdirs()
                target.bytes = content
            }
        }
    }

    private void exportMethods(Project project, MergeCommit mergeCommit) {
        List<MergeMethodModifications> methods = methodsOfEachMergeCommit.remove(mergeCommit.getSHA())
        for (ModifiedLinesExporter exporter : getExporters()) {
            exporter.export(project, mergeCommit, methods)
        }
    }

    // created once, since the json exporter keeps all the scenarios to rewrite its file
    private synchronized List<ModifiedLinesExporter> getExporters() {
        if (exporters == null) {
            String outputPath = arguments.getOutputPath()
            exporters = [new JsonModifiedLinesExporter(outputPath), new CsvModifiedLinesExporter(outputPath)]
        }
        return exporters
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

        methodsOfEachMergeCommit.get(mergeCommit.getSHA()) << new MergeMethodModifications(className: className, method: modifiedDeclarationSignature,
                leftAddedLines: new TreeSet<>(leftAddedLines), leftDeletedLines: new TreeSet<>(leftDeletedLines),
                rightAddedLines: new TreeSet<>(rightAddedLines), rightDeletedLines: new TreeSet<>(rightDeletedLines))

        if (arguments.isPushCommandActive()) {
            String remoteURL = arguments.getResultsRemoteRepositoryURL()
            String link = "${remoteURL}/commit/${mergeCommit.getSHA()}#diff-something" // Simplification
            experimentalDataFileWithLinks << "${project.getName()};${mergeCommit.getSHA()};${className};${modifiedDeclarationSignature};${leftAddedLines};${leftDeletedLines};${rightAddedLines};${rightDeletedLines};${link}\n"
        }
    }

}
