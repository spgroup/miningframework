package services.dataCollectors.modifiedLinesCollector.exporters

import project.MergeCommit
import project.Project
import services.dataCollectors.modifiedLinesCollector.MergeMethodModifications

/**
 * @provides: a [outputPath]/data/results-with-build-information.csv file, read by GenerateSootInputFilesOutputProcessor
 * and RunSootAnalysisOutputProcessor, with one line per modified method:
 * project;merge commit;className;method;left modifications;has_build;left deletions;right modifications;right deletions
 */
class CsvModifiedLinesExporter implements ModifiedLinesExporter {

    private final File file

    CsvModifiedLinesExporter(String outputPath) {
        file = new File(outputPath, 'data/results-with-build-information.csv')
        file.parentFile.mkdirs()
        if (!file.exists()) {
            file << 'project;merge commit;className;method;left modifications;has_build;left deletions;right modifications;right deletions\n'
        }
    }

    synchronized void export(Project project, MergeCommit mergeCommit, List<MergeMethodModifications> methods) {
        for (MergeMethodModifications method : methods) {
            file << [project.getName(), mergeCommit.getSHA(), clean(method.className), clean(method.method),
                     lines(method.leftAddedLines), 'true', lines(method.leftDeletedLines), lines(method.rightAddedLines),
                     lines(method.rightDeletedLines)].join(';') + '\n'
        }
    }

    // class and method also name the folders of the soot input files
    static String clean(String name) {
        return name.replaceAll(/[\s+^?<>|]/, '')
    }

    private static String lines(Set<Integer> lines) {
        return "[${lines.join(',')}]"
    }
}
