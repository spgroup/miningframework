package services.modifiedLinesCollector

import java.util.HashSet

class DiffJParser {

    private final String NEW_LINES_MARKER = "---";
    private final int INT_ZERO = 0;
    private final int INT_ONE = 1;

    public List<ModifiedLine> parse (List<String> lines) {
        def result = new ArrayList<ModifiedLine>();

        if (lines.size() > INT_ZERO) {
            def iterator = lines.iterator();
            
            iterator.next();
            def headerLine = iterator.next();
            def lineNumbers = getLineNumbers(headerLine);

            int begin = lineNumbers[INT_ZERO];
            int end = lineNumbers[INT_ONE];
                        
            skipIteratorToLinesContents(iterator)

            for (int i = begin; i <= end; i++) {
                def lineContent = parseLineContent(iterator.next());

                def modLine = new ModifiedLine(i, lineContent, ModifiedLine.ModificationType.Added)
                result.add(modLine)
            }

        }

        return result;
    }

    private String parseLineContent(String line) {
        return line.substring(1).strip();
    }

    private void skipIteratorToLinesContents(Iterator<ModifiedLine> iterator) {
        while(iterator.next() != NEW_LINES_MARKER);
    }

    private int[] getLineNumbers (String headerLine) {
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