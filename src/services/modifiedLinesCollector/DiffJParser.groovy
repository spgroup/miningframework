package services.modifiedLinesCollector

import java.util.HashSet

import java.util.regex.Pattern
import java.util.regex.Matcher

class DiffJParser {

    private final String NEW_LINES_MARKER = "---";
    private final int INT_ZERO = 0;
    private final int INT_ONE = 1;

    private final Pattern CHANGE_HEADER_PATTERN = Pattern.compile(".* code added in .*")

    public List<ModifiedLine> parse (List<String> lines) {
        def result = new ArrayList<ModifiedLine>();
        def iterator = lines.iterator();
           
        while(iterator.hasNext()) {
            // get next line
            def line = iterator.next();

            // checks it if its a header line e.g: 4a4 code added in method()
            if (isHeaderLine(line)) {
                def lineNumbers = getLineNumbersRange(line);
                
                int begin = lineNumbers[INT_ZERO];
                int end = lineNumbers[INT_ONE];
                
                // move the iterator util it finds a predefined marker
                skipIteratorToLinesContents(iterator)

                // gets the contents of the lines and adds then to the list
                for (int i = begin; i <= end; i++) {
                    def lineContent = parseLineContent(iterator.next());

                    def modLine = new ModifiedLine(i, lineContent, ModifiedLine.ModificationType.Added)
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
        return line.substring(INT_ONE).strip();
    }

    private void skipIteratorToLinesContents(Iterator<ModifiedLine> iterator) {
        while(iterator.next() != NEW_LINES_MARKER);
    }

    private int[] getLineNumbersRange(String headerLine) {
        def splittedHeaderLine = headerLine.split(" ");
        def modifiedLinesPart = splittedHeaderLine[INT_ZERO]
        def splittedModifiedLinesPart = modifiedLinesPart.split("a")
        
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