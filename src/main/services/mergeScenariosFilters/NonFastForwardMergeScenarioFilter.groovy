package services.mergeScenariosFilters

import java.nio.file.Path
import java.util.stream.Collectors
import services.util.Utils

class NonFastForwardMergeScenarioFilter {
    public static List<Path> applyFilter(List<Path> mergeScenarios) {
        return mergeScenarios.stream().filter(this::isNonFastForwardMergeScenario).collect(Collectors.toList())
    }

    private static boolean isNonFastForwardMergeScenario(Path mergeScenario) {
        Path leftFile = getInvolvedFile(mergeScenario, 'left')
        Path baseFile = getInvolvedFile(mergeScenario, 'base')
        Path rightFile = getInvolvedFile(mergeScenario, 'right')

        if (leftFile.getText() == baseFile.getText())
            return false

        if (rightFile.getText() == baseFile.getText())
            return false

        if (leftFile.getText() == rightFile.getText())
            return false

        return true
    }

    private static Path getInvolvedFile(Path mergeScenario, String fileName) {
        return mergeScenario.resolve(Utils.getfileNameWithExtension(fileName)).toAbsolutePath()
    }
}
