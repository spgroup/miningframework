package services.dataCollectors.MergirafAndSepMergeCollector

import java.nio.file.Path

class DataAnalyser {
    static List<MergeSummary> analyseMerges(List<Path> filesQuadruplePaths) {
        return filesQuadruplePaths.stream().map(MergeSummary::new).toList()
    }
}
