package services.modifiedLinesCollector

import java.util.regex.Pattern
import java.util.regex.Matcher

class DiffJParser {

    private final int INT_ZERO = 0;
    private final int INT_ONE = 1;

    private final String ADDED_FLAG = "a";
    private final String CHANGED_FLAG = "c";
    private final String REMOVED_FLAG = "d";

    private final String FLAGS_REGEX = "(${ADDED_FLAG}|${CHANGED_FLAG}|${REMOVED_FLAG})"
    private final Pattern METHOD_CHANGE_HEADER_REGEX = Pattern.compile("^.* code (added|changed|removed) in .*")

    public Map<String, int[]> parse (List<String> lines) {
        def result = new HashMap<String, int[]>();
        
        def iterator = lines.iterator();
        if (iterator.hasNext()) {
            iterator.next();
        }

        while(iterator.hasNext()) {
            def line = iterator.next();
            
            if (isMethodChangeLine(line)) {
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

        }

        return result;
    }

    private boolean isMethodChangeLine(String line) {
        Matcher matcher = METHOD_CHANGE_HEADER_REGEX.matcher(line);
        
        return matcher.find();
    }

    private String getMethodName(String headerLine) {
        return headerLine.replaceFirst(/^.* code (added|changed|removed) in /, "");
    }

    private int[] getModifiedLinesRange(String headerLine) {
        String[] splittedHeaderLine = headerLine.split(":");
        String modifiedLinesPart = splittedHeaderLine[INT_ZERO]
        String[] splittedModifiedLinesPart = modifiedLinesPart.split(/${FLAGS_REGEX}/)

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