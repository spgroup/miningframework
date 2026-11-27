package services.dataCollectors.modifiedLinesCollector.exporters

import project.MergeCommit
import project.Project
import services.dataCollectors.modifiedLinesCollector.MergeMethodModifications

/**
 * Exports the modified lines of a merge scenario.
 */
interface ModifiedLinesExporter {

    void export(Project project, MergeCommit mergeCommit, List<MergeMethodModifications> methods)

}
