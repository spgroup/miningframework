package services.dataCollectors.modifiedLinesCollector.exporters

import groovy.json.JsonOutput
import project.MergeCommit
import project.Project
import services.dataCollectors.modifiedLinesCollector.MergeMethodModifications
import util.MergeBuildHelper

/**
 * @provides: a [outputPath]/data/results-with-build-information.json file with the same information as
 * CsvModifiedLinesExporter, but with one object per merge scenario holding all its modified methods
 */
class JsonModifiedLinesExporter implements ModifiedLinesExporter {

    private final String outputPath
    private final File file
    private final List<Map> scenarios = []

    JsonModifiedLinesExporter(String outputPath) {
        this.outputPath = outputPath
        file = new File(outputPath, 'data/results-with-build-information.json')
        file.parentFile.mkdirs()
    }

    synchronized void export(Project project, MergeCommit mergeCommit, List<MergeMethodModifications> methods) {
        scenarios << [
                project        : project.getName(),
                commits        : [merge: mergeCommit.getSHA(), left: mergeCommit.getLeftSHA(),
                                  right: mergeCommit.getRightSHA(), base: mergeCommit.getAncestorSHA()],
                hasBuild       : hasBuild(project, mergeCommit),
                modifiedMethods: methods.collect {
                    [className         : it.className,
                     method            : it.method,
                     leftModifications : it.leftAddedLines as List,
                     leftDeletions     : it.leftDeletedLines as List,
                     rightModifications: it.rightAddedLines as List,
                     rightDeletions    : it.rightDeletedLines as List]
                },
        ]
        // rewritten at each scenario, so the file is always a valid list
        file.text = JsonOutput.prettyPrint(JsonOutput.toJson(scenarios))
    }

    private boolean hasBuild(Project project, MergeCommit mergeCommit) {
        return MergeBuildHelper.getMergeBuild(outputPath, project.getName(), mergeCommit.getSHA()).exists()
    }
}
