package util

import java.util.stream.Collectors

class CsvUtils {
    /**
     * Converts a list into a CSV row.
     * @param List<String> items
     * @return The row separated by commas
     */
    static String toCsvRepresentation(List<String> items) {
        return items.join(',').replaceAll('\\\\', '/')
    }

    /**
     * Collects a Stream of Strings into a single String separated by line breaks
     * @param lines
     * @return
     */
    static asLines() {
        return Collectors.joining(System.lineSeparator())
    }
}
