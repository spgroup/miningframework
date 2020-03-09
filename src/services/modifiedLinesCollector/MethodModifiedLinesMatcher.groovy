package services.modifiedLinesCollector

public class MethodModifiedLinesMatcher {

    Set<ModifiedMethod> matchModifiedMethodsAndLines(Map<String, int[]> modifiedMethods, List<ModifiedLine> modifiedLines) {
        def methodSet = new HashSet<ModifiedMethod>();
        
        for (def method: modifiedMethods.entrySet()) {
            String methodSignature = method.getKey();
            int[] lines = method.getValue();

            def lineSet = new HashSet<ModifiedLine>();

            for (def lineNumber: lines) {
                def diffLine = findModifiedLineByNumber(lineNumber, modifiedLines);
            
                if (diffLine != null) {
                    lineSet.add(diffLine);
                }
            }

            methodSet.add(new ModifiedMethod(methodSignature, lineSet));
        }

        return methodSet;
    }

    public ModifiedLine findModifiedLineByNumber(int lineNumber, List<ModifiedLine> modifiedLines) {
        for (def modifiedLine: modifiedLines) {
            if (modifiedLine.getNumber() == lineNumber) {
                return modifiedLine;
            }
        }
        return null;
    }

}