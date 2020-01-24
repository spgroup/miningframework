package services.modifiedLinesCollector

import java.util.HashSet

import java.util.regex.Pattern
import java.util.regex.Matcher

class DiffParser {

    private final String NEW_LINES_MARKER = "---";
    private final int INT_ZERO = 0;
    private final int INT_ONE = 1;

    private final Pattern CHANGE_HEADER_PATTERN = Pattern.compile("([0-9]+(,[0-9]+)?(a|d|c)[0-9]+(,[0-9]+)?)\$")

    public List<ModifiedLine> parse (List<String> lines) {
        def result = new ArrayList<ModifiedLine>();
        def iterator = lines.iterator();
        
        while(iterator.hasNext()) {
            // get next line
            def line = iterator.next().trim();
            // checks it if its a header line e.g: 4a4 code added in method()
            if (isHeaderLine(line)) {
                def changeType = getChangeType(line);
                def lineNumbers = getLineNumbersRange(line);
                
                int begin = lineNumbers[INT_ZERO];
                int end = lineNumbers[INT_ONE];

                if (changeType == ModifiedLine.ModificationType.Changed) {
                    while (iterator.next().trim() != NEW_LINES_MARKER) {}
                }
                
                // gets the contents of the lines and adds then to the list
                for (int i = begin; i <= end; i++) {
                    def lineContent = parseLineContent(iterator.next());

                    def modLine = new ModifiedLine(i, lineContent, changeType)
                    result.add(modLine)
                }

            }
        }

        return result;
    }

    private boolean isHeaderLine(String line) {
        Matcher matcher = CHANGE_HEADER_PATTERN.matcher(line);
        
        return matcher.find();
    }

    private String parseLineContent(String line) {
        return line.substring(INT_ONE).trim();
    }

    private ModifiedLine.ModificationType getChangeType(String headerLine) {
        if (headerLine.contains("a")) {
            return ModifiedLine.ModificationType.Added;
        } else if (headerLine.contains("c")) {
            return ModifiedLine.ModificationType.Changed;
        } else {
            return ModifiedLine.ModificationType.Removed;
        }
    }

    private int[] getLineNumbersRange(String headerLine) {
        def splittedHeaderLine = headerLine.split(" ");
        def modifiedLinesPart = splittedHeaderLine[INT_ZERO]
        def splittedModifiedLinesPart = modifiedLinesPart.split(/[ac]/)
        
        def newLines = splittedModifiedLinesPart[INT_ONE]
        def splittedNewLines = newLines.split(",")

        int begin = Integer.parseInt(splittedNewLines[INT_ZERO])
        int end = begin;
        if (splittedNewLines.size() > INT_ONE) {
            end = Integer.parseInt(splittedNewLines[INT_ONE]);
        }

        return [begin, end]
    }


}