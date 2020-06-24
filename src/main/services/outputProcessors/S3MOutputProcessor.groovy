
package services.outputProcessors

import interfaces.OutputProcessor
import services.util.Utils

import java.nio.file.Path
import java.nio.file.Paths

class S3MOutputProcessor implements OutputProcessor {

    private static final Path ANALYSIS_REPOSITORY_PATH = Paths.get('../merge-tools')
    static final String ANALYSIS_REMOTE_URL = "https://github.com/jvcoutinho/merge-tools/tree/master/s3m-handlers-analysis"

    @Override
    void processOutput() {
        stageAndPushData()
        println 'Pushed data to remote analysis repository'
    }

    private static void stageAndPushData() {
        // Stage changes.
        Utils.runGitCommand(ANALYSIS_REPOSITORY_PATH, 'add', '.')

        // Commit changes.
        Utils.runGitCommand(ANALYSIS_REPOSITORY_PATH, 'commit', '-m', 'Collected data')

        // Push changes.
        Utils.runGitCommand(ANALYSIS_REPOSITORY_PATH, 'push', '--force-with-lease')
    }


}
