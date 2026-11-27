package services.dataCollectors.modifiedLinesCollector.exporters

import groovy.json.JsonOutput
import project.MergeCommit
import project.Project
import services.dataCollectors.modifiedLinesCollector.MergeMethodModifications
import util.MergeBuildHelper

import java.nio.charset.StandardCharsets

/**
 * @provides: a [outputPath]/data/results-with-build-information.json file with the same information as
 * CsvModifiedLinesExporter, but with one object per merge scenario holding all its modified methods
 */
class JsonModifiedLinesExporter implements ModifiedLinesExporter {

    private static final String END = '\n]'
    private static final String INDENT = '    ' // as JsonOutput.prettyPrint indents the elements of a list

    private final String outputPath
    private final File file
    private boolean empty = true

    JsonModifiedLinesExporter(String outputPath) {
        this.outputPath = outputPath
        file = new File(outputPath, 'data/results-with-build-information.json')
        file.parentFile.mkdirs()
        file.setText('[' + END, 'UTF-8')
    }

    synchronized void export(Project project, MergeCommit mergeCommit, List<MergeMethodModifications> methods) {
        Map scenario = [
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
        append(scenario)
    }

    /*
     * Writes the scenario over the "]" that closes the list and closes it again, so the file is always a valid list
     * and each export writes only its scenario. The text is the one of JsonOutput.prettyPrint of the whole list.
     */
    private void append(Map scenario) {
        String element = JsonOutput.prettyPrint(JsonOutput.toJson(scenario)).readLines().collect { INDENT + it }.join('\n')
        String text = (empty ? '\n' : ',\n') + element + END

        RandomAccessFile output = new RandomAccessFile(file, 'rw')
        try {
            long position = output.length() - END.length()
            output.seek(position)
            output.write(text.getBytes(StandardCharsets.UTF_8))
        } finally {
            output.close()
        }
        empty = false
    }

    private boolean hasBuild(Project project, MergeCommit mergeCommit) {
        return MergeBuildHelper.hasMergeBuild(MergeBuildHelper.getMergeBuildFolder(outputPath, project.getName(), mergeCommit.getSHA()))
    }
}
