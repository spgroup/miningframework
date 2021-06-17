package services.util

import org.apache.commons.io.FileUtils
@Grab(group = 'commons-io', module = 'commons-io', version = '2.6')
import org.apache.commons.lang3.StringUtils

import java.nio.charset.Charset
import java.nio.file.Path

class MergeConflict {

    enum ConflictArea {
        None,
        Left,
        Base,
        Right
    }

    public static MINE_CONFLICT_MARKER = "<<<<<<<MINE"
    public static BASE_CONFLICT_MARKER = "|||||||"
    public static YOURS_CONFLICT_MARKER = ">>>>>>>YOURS"
    public static CHANGE_CONFLICT_MARKER = "======="
    public static SIMPLE_CONFLICT_MARKER = "<<<<<<<"

    private String left
    private String right

    MergeConflict(String left, String right) {
        this.left = left
        this.right = right
    }

    @Override
    boolean equals(Object o) {
        return StringUtils.deleteWhitespace(left) == StringUtils.deleteWhitespace(((MergeConflict) o).left)
                && StringUtils.deleteWhitespace(right) == StringUtils.deleteWhitespace(((MergeConflict) o).right)
    }

    /**
     * @param file
     * @return the set of merge conflicts present in the given file
     */
    static Set<MergeConflict> extractMergeConflicts(Path file) {
        Set<MergeConflict> mergeConflicts = new HashSet<MergeConflict>()

        StringBuilder leftConflictingContent = new StringBuilder()
        StringBuilder rightConflictingContent = new StringBuilder()

        ConflictArea conflictArea
        conflictArea = ConflictArea.None

        Iterator<String> mergeCodeLines = FileUtils.readLines(file.toFile(), Charset.defaultCharset()).iterator()
        while (mergeCodeLines.hasNext()) {
            String line = mergeCodeLines.next()

            /* See the following conditionals as a state machine. */
            if (StringUtils.deleteWhitespace(line).contains(MINE_CONFLICT_MARKER) && conflictArea == ConflictArea.None) {
                conflictArea = ConflictArea.Left
            } else if (StringUtils.deleteWhitespace(line).contains(CHANGE_CONFLICT_MARKER) && conflictArea == ConflictArea.Left) {
                conflictArea = ConflictArea.Right
            } else if (StringUtils.deleteWhitespace(line).contains(YOURS_CONFLICT_MARKER) && conflictArea == ConflictArea.Right) {
                mergeConflicts.add(new MergeConflict(leftConflictingContent.toString(), rightConflictingContent.toString()))
                conflictArea = ConflictArea.None
            } else {
                switch (conflictArea) {
                    case ConflictArea.Left:
                        leftConflictingContent.append(line).append('\n')
                        break
                    case ConflictArea.Right:
                        rightConflictingContent.append(line).append('\n')
                        break
                    default: // not in conflict area
                        break
                }
            }
        }
        return mergeConflicts
    }

    static void removeBaseFromConflicts(Path file) {
        Iterator<String> mergeCodeLines = FileUtils.readLines(file.toFile(), Charset.defaultCharset()).iterator()

        List<String> newMergeCodeLines = []
        ConflictArea conflictArea = ConflictArea.None

        while (mergeCodeLines.hasNext()) {
            String line = mergeCodeLines.next()
            if (StringUtils.deleteWhitespace(line).startsWith(MINE_CONFLICT_MARKER)) {
                conflictArea = ConflictArea.Left
            } else if (StringUtils.deleteWhitespace(line).startsWith(BASE_CONFLICT_MARKER)) {
                conflictArea = ConflictArea.Base
            } else if (StringUtils.deleteWhitespace(line).startsWith(CHANGE_CONFLICT_MARKER)) {
                conflictArea = ConflictArea.Right
            } else if (StringUtils.deleteWhitespace(line).startsWith(YOURS_CONFLICT_MARKER)) {
                conflictArea = ConflictArea.None
            }

            if (conflictArea != ConflictArea.Base) {
                newMergeCodeLines.add(line)
            }
        }

        FileUtils.writeLines(file.toFile(), newMergeCodeLines)
    }

    public static int getConflictsNumber(Path file) {
        int conflictCount = 0
        Iterator<String> mergeCodeLines = FileUtils.readLines(file.toFile(), Charset.defaultCharset()).iterator()
        while (mergeCodeLines.hasNext()) {
            String line = mergeCodeLines.next()
            if (line.startsWith(SIMPLE_CONFLICT_MARKER)) {
                conflictCount += 1
            }
        }
        return conflictCount
    }

}
