package services.dataCollectors.modifiedLinesCollector

/**
 * A method of the merge with the lines added and deleted by left and by right, numbered in the merge revision.
 */
class MergeMethodModifications {
    String className
    String method
    Set<Integer> leftAddedLines = new TreeSet<>()
    Set<Integer> leftDeletedLines = new TreeSet<>()
    Set<Integer> rightAddedLines = new TreeSet<>()
    Set<Integer> rightDeletedLines = new TreeSet<>()
}
