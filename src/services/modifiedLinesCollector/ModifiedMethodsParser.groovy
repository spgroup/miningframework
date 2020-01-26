package services.modifiedLinesCollector

import java.util.regex.Pattern
import java.util.regex.Matcher

class ModifiedMethodsParser {

    private final int INT_ZERO = 0;
    private final int INT_ONE = 1;

    public Map<String, List> parse (List<String> lines) {
        def result = new HashMap<String, int[]>();
        
        def iterator = lines.iterator();
        iterator.next();

        while(iterator.hasNext()) {
            def line = iterator.next();

            def modifiedLinesRange = getModifiedLinesRange(line);
            def begin = modifiedLinesRange[INT_ZERO];
            def end = modifiedLinesRange[INT_ONE];

            def lineList = [];
            for (int number = begin; number <= end; number++) {
                lineList.add(number);
            }

            def methodName = getMethodName(line);
            if (result.containsKey(methodName)) {
                result.put(methodName, result.get(methodName) + lineList)
            } else {
                result.put(methodName, lineList);
            }
        }

        return result;
    }

    private String getMethodName(String headerLine) {
        String[] splittedHeaderLine = headerLine.split(" ");

        return splittedHeaderLine[splittedHeaderLine.size() - 1];
    }

    private int[] getModifiedLinesRange(String headerLine) {
        String[] splittedHeaderLine = headerLine.split(":");
        String modifiedLinesPart = splittedHeaderLine[INT_ZERO]
        String[] splittedModifiedLinesPart = modifiedLinesPart.split("a")
    
        return getNumbersRange(splittedModifiedLinesPart[INT_ONE]);
    }

    private int[] getNumbersRange(String rangeString) {        
        def splittedNewLines = rangeString.split(",")

        int begin = Integer.parseInt(splittedNewLines[INT_ZERO])
        int end = begin;
        if (splittedNewLines.size() > INT_ONE) {
            end = Integer.parseInt(splittedNewLines[INT_ONE]);
        }

        return [begin, end]
    }

}