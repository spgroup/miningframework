package services.modifiedLinesCollector

import services.modifiedLinesCollector.exceptions.TextualDiffParsingException

import java.util.HashSet

import java.util.regex.Pattern
import java.util.regex.Matcher

class TextualDiffParser {

    private final String NEW_LINES_MARKER = "---";
    private final int INT_ZERO = 0;
    private final int INT_ONE = 1;

    private final String ADDED_FLAG = "a";
    private final String CHANGED_FLAG = "c";
    private final String REMOVED_FLAG = "d";
    private final String FLAGS_REGEX = "(${ADDED_FLAG}|${CHANGED_FLAG}|${REMOVED_FLAG})"

    private final Pattern HEADER_PATTERN = Pattern.compile("([0-9]+(,[0-9]+)?${FLAGS_REGEX}[0-9]+(,[0-9]+)?)\$");

    public List<ModifiedLine> parse (List<String> lines) {
        def result = new ArrayList<ModifiedLine>();
        def iterator = lines.iterator();
        
        while(iterator.hasNext()) {
            // get next line
            def line = iterator.next().trim();
            // checks it if its a header line e.g: 4a4 code added in method()
            if (isHeaderLine(line)) {
                def changeType = getChangeType(line);
                def lineNumbers = getModifiedLinesRange(line);
                
                int begin = lineNumbers[INT_ZERO];
                int end = lineNumbers[INT_ONE];

                if (changeType == ModifiedLine.ModificationType.Changed) {
                    dropUntilFindMarker(iterator)
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

    private void dropUntilFindMarker(Iterator<String> iterator) {
        while (iterator.next().trim() != NEW_LINES_MARKER) {
        }
    }

    private boolean isHeaderLine(String line) {
        Matcher matcher = HEADER_PATTERN.matcher(line);
        
        return matcher.find();
    }

    private String parseLineContent(String line) {
        return line.substring(INT_ONE).trim();
    }

    private ModifiedLine.ModificationType getChangeType(String headerLine) {
        if (headerLine.contains(ADDED_FLAG)) {
            return ModifiedLine.ModificationType.Added;
        } else if (headerLine.contains(CHANGED_FLAG)) {
            return ModifiedLine.ModificationType.Changed;
        } else {
            return ModifiedLine.ModificationType.Removed;
        }
    }

    private int[] getModifiedLinesRange(String headerLine) {
        String[] splittedHeaderLine = headerLine.split(" ");
        String modifiedLinesPart = splittedHeaderLine[INT_ZERO]
        String[] splittedModifiedLinesPart = modifiedLinesPart.split(/${FLAGS_REGEX}/)

        if (splittedModifiedLinesPart.length <= INT_ONE) {
            throw new TextualDiffParsingException("a, c or d", modifiedLinesPart);
        }
        
        ModifiedLine.ModificationType changeType = getChangeType(headerLine);

        String rangeString = splittedModifiedLinesPart[INT_ONE]
    
        return getNumbersRange(rangeString);
    }

    private int[] getNumbersRange(String rangeString) {
        try {
            def splittedNewLines = rangeString.split(",")

            int begin = Integer.parseInt(splittedNewLines[INT_ZERO])
            int end = begin;
            if (splittedNewLines.size() > INT_ONE) {
                end = Integer.parseInt(splittedNewLines[INT_ONE]);
            }

            return [begin, end]
        } catch (NumberFormatException e) {
            throw new TextualDiffParsingException("a number range", rangeString);
        }
    }


}