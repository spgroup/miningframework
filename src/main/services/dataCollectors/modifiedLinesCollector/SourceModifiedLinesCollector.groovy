package services.dataCollectors.modifiedLinesCollector

import groovy.io.FileType
import interfaces.DataCollector
import org.apache.commons.io.FileUtils
import project.MergeCommit
import project.Project
import services.dataCollectors.modifiedLinesCollector.exporters.ModifiedLinesExporter
import util.MergeBuildHelper

/**
 * Same collection as the git based one (ModifiedLinesCollector, and SSM's ModifiedLinesManager), for merge scenarios
 * that only have the source of the modified files: every method modified by left or by right, with the lines of merge
 * that each one modified (diffj and textual diff from base). The entrypoints of the analyses are not chosen here.
 *
 * @requires: that the project path is a folder with source/{base,left,right,merge}/ holding the modified .java files
 * at the same relative paths and jars/merge.jar (the build); diffj in the dependencies folder and diff installed
 * @provides: the modified lines to the exporters, and the merge jar as the build of the scenario at
 * [outputPath]/files/[project]/[merge commit]/original-without-dependencies/merge/build.jar, with a copy of the
 * source/ and jars/ folders of the scenario beside it
 */
class SourceModifiedLinesCollector implements DataCollector {

    private final List<ModifiedLinesExporter> exporters
    private final String outputPath
    private final ModifiedMethodsHelper modifiedMethodsHelper

    SourceModifiedLinesCollector(List<ModifiedLinesExporter> exporters, String outputPath, String dependenciesPath = 'dependencies') {
        this.exporters = exporters
        this.outputPath = outputPath
        this.modifiedMethodsHelper = new ModifiedMethodsHelper('diffj.jar', dependenciesPath)
    }

    void collectData(Project project, MergeCommit mergeCommit) {
        File source = new File(project.getPath(), 'source')
        List<MergeMethodModifications> methods = []

        new File(source, 'merge').eachFileRecurse(FileType.FILES) { File mergeFile ->
            String path = new File(source, 'merge').toURI().relativize(mergeFile.toURI()).path
            File base = new File(source, "base/${path}")
            if (!path.endsWith('.java') || !base.exists()) {
                return
            }
            // a parent without the file did not modify it
            File left = new File(source, "left/${path}").with { it.exists() ? it : base }
            File right = new File(source, "right/${path}").with { it.exists() ? it : base }

            Map<String, ModifiedMethod> leftMethods = modifiedMethodsHelper.getModifiedMethods(base, left).collectEntries { [(it.getSignature()): it] }
            Map<String, ModifiedMethod> rightMethods = modifiedMethodsHelper.getModifiedMethods(base, right).collectEntries { [(it.getSignature()): it] }
            String className = className(mergeFile)
            for (ModifiedMethod mergeMethod : modifiedMethodsHelper.getModifiedMethods(base, mergeFile)) {
                if (leftMethods.containsKey(mergeMethod.getSignature()) || rightMethods.containsKey(mergeMethod.getSignature())) {
                    methods << modifications(className, mergeMethod, leftMethods[mergeMethod.getSignature()], rightMethods[mergeMethod.getSignature()])
                }
            }
        }

        File scenarioFolder = new File(outputPath, "files/${project.getName()}/${mergeCommit.getSHA()}")
        File build = MergeBuildHelper.getMergeBuild(outputPath, project.getName(), mergeCommit.getSHA())
        build.parentFile.mkdirs()
        build.bytes = new File(project.getPath(), 'jars/merge.jar').bytes

        // the scenario as the dataset has it, to use it again later
        FileUtils.copyDirectory(new File(project.getPath(), 'source'), new File(scenarioFolder, 'source'))
        FileUtils.copyDirectory(new File(project.getPath(), 'jars'), new File(scenarioFolder, 'jars'))

        exporters.each { it.export(project, mergeCommit, methods) }
        println "${project.getName()} - SourceModifiedLinesCollector collection finished"
    }

    // as ModifiedLinesCollectorAbstract.collectMethodData: the modified lines of merge that each parent also modified
    private static MergeMethodModifications modifications(String className, ModifiedMethod mergeMethod, ModifiedMethod leftMethod, ModifiedMethod rightMethod) {
        MergeMethodModifications method = new MergeMethodModifications(className: className, method: mergeMethod.getSignature())
        for (ModifiedLine line : mergeMethod.getModifiedLines()) {
            boolean removed = line.getType() == ModifiedLine.ModificationType.Removed
            if (leftMethod?.getModifiedLines()?.contains(line)) {
                (removed ? method.leftDeletedLines : method.leftAddedLines).add(line.getNumber())
            }
            if (rightMethod?.getModifiedLines()?.contains(line)) {
                (removed ? method.rightDeletedLines : method.rightAddedLines).add(line.getNumber())
            }
        }
        return method
    }

    // as TypeNameHelper.getFullyQualifiedName: package of the file and its name
    private static String className(File file) {
        def matcher = file.text =~ /(?m)^\s*package\s+([\w.]+)\s*;/
        String name = file.name - '.java'
        return matcher.find() ? "${matcher.group(1)}.${name}" : name
    }
}
