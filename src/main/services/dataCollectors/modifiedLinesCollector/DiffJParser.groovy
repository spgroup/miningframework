package services.dataCollectors.modifiedLinesCollector

import services.dataCollectors.modifiedLinesCollector.exceptions.DiffJParsingException

import java.util.regex.Matcher
import java.util.regex.Pattern

class DiffJParser {

    private final int INT_ZERO = 0;
    private final int INT_ONE = 1;

    private final String ADDED_FLAG = "a";
    private final String CHANGED_FLAG = "c";
    private final String REMOVED_FLAG = "d";

    private final String FLAGS_REGEX = "(${ADDED_FLAG}|${CHANGED_FLAG}|${REMOVED_FLAG})"
    private final Pattern STATEMENT_CHANGE_HEADER_REGEX = Pattern.compile("^.* code (added|changed|removed) in .*")
    private final Pattern METHOD_DECLARATION_CHANGE_HEADER_REGEX = Pattern.compile("^.* method (added|changed|removed): .*")

    public Map<String, int[]> parse (List<String> lines) {
        def result = new HashMap<String, int[]>();
        
        def iterator = lines.iterator();
        dropFirstLine(iterator);

        while(iterator.hasNext()) {
            def line = iterator.next();
            
            if (isMethodChangeLine(line)) {
                def modifiedLinesRange = getModifiedLinesRange(line);
                def begin = modifiedLinesRange[INT_ZERO];
                def end = modifiedLinesRange[INT_ONE];

                def lineList = mountModifiedLinesList(begin, end);

                def methodName = getMethodName(line);

                addLineListToMethodKey(result, methodName, lineList);
            }

        }

        return result;
    }

    List<ModifiedMethod> parseAllModifiedMethods(List<String> diffJResult) {
        List<ModifiedMethod> modifiedMethods = new ArrayList<>();

        for (int i = 1; i < diffJResult.size(); i++) {
            String line = diffJResult.get(i);
            String methodSignature = line.replaceAll("\\(changed\\)|\\(added\\)", "").trim();
            modifiedMethods.add(new ModifiedMethod(methodSignature));
        }

        return modifiedMethods;
    }

    private void addLineListToMethodKey(Map<String, int[]> result, String methodName, List<Integer> lineList) {
        if (result.containsKey(methodName)) {
            result.put(methodName, result.get(methodName) + lineList)
        } else {
            result.put(methodName, lineList);
        }
    }

    private List<Integer> mountModifiedLinesList(int begin, int end) {
        def lineList = [];
        for (int number = begin; number <= end; number++) {
            lineList.add(number);
        }
        return lineList;
    }

    private void dropFirstLine(Iterator<String> iterator) {
        if (iterator.hasNext()) {
            iterator.next();
        }
    }

    private boolean isMethodChangeLine(String line) {
        Matcher codeMatcher = STATEMENT_CHANGE_HEADER_REGEX.matcher(line);

        Matcher methodMatcher = METHOD_DECLARATION_CHANGE_HEADER_REGEX.matcher(line);

        return codeMatcher.find() || methodMatcher.find();
    }

    private String getMethodName(String headerLine) {
        return headerLine.replaceFirst(/^.* code (added|changed|removed) in /, "").replaceFirst(/^.* method (added|changed|removed): /, "");
    }

    private int[] getModifiedLinesRange(String headerLine) {
        String[] splittedHeaderLine = headerLine.split(":");
        if (splittedHeaderLine.length <= 1) {
            throw new DiffJParsingException("\":\"", headerLine);
        }

        String modifiedLinesPart = splittedHeaderLine[INT_ZERO]
        String[] splittedModifiedLinesPart = modifiedLinesPart.split(/${FLAGS_REGEX}/)
        if (splittedModifiedLinesPart.length <= 1) {
            throw new DiffJParsingException("\"a, c or d\"", headerLine);
        }

        return getNumbersRange(splittedModifiedLinesPart[INT_ONE]);
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
            throw new DiffJParsingException("number range", rangeString);
        }
    }

}
