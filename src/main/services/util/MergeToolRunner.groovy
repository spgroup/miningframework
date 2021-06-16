package services.util

import java.nio.file.Path

abstract class MergeToolRunner {

    abstract void collectResults(List<Path> filesQuadruplePaths);

    protected Path getContributionFile(Path filesQuadruplePath, String contributionFileName) {
        return filesQuadruplePath.resolve("${contributionFileName}.java").toAbsolutePath()
    }

}