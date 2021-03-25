package services.mergeScenariosFilters

import java.nio.file.Path
import java.util.stream.Collectors

class NonFastForwardMergeScenarioFilter {
    public static List<Path> applyFilter(List<Path> mergeScenarios) {
        return mergeScenarios.stream().filter(this::isNonFastForwardMergeScenario).collect(Collectors.toList())
    }

    private static boolean isNonFastForwardMergeScenario(Path mergeScenario) {
        Path leftFile = getInvolvedFile(mergeScenario, 'left')
        Path baseFile = getInvolvedFile(mergeScenario, 'base')
        Path rightFile = getInvolvedFile(mergeScenario, 'right')

        return leftFile.getText() != baseFile.getText() && rightFile.getText() != baseFile.getText()
    }

    private static Path getInvolvedFile(Path mergeScenario, String fileName) {
        return mergeScenario.resolve("${fileName}.java").toAbsolutePath()
    }
}
