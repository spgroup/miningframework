package services.dataCollectors.modifiedLinesCollector

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticBlockModifiedLinesMatcher {
    private String CONST_INITIALIZER_DECLARATION = "static-";
    Set<ModifiedStaticBlock> matchModifiedStaticBlocksASTLines(Map<String, String> collectionsStaticBlocks, List<ModifiedLine> modifiedLines) {
        def staticBlockSet = new HashSet<ModifiedStaticBlock>();

        if (collectionsStaticBlocks.entrySet().size() > 0) {
            for (def initializerDeclaration : collectionsStaticBlocks.entrySet()) {
                String identifier = initializerDeclaration.getKey();
                //String identifier = CONST_INITIALIZER_DECLARATION + initializerDeclaration.getKey();
                String blockedStatic = initializerDeclaration.getValue();

                def lineSet = new HashSet<ModifiedLine>();
                def diffLineText = findModifiedLineByText(identifier,blockedStatic, modifiedLines);
                identifier = getIdentifierNumber(identifier);
                if (diffLineText != null) {
                    lineSet.add(diffLineText);
                    staticBlockSet.add(new ModifiedStaticBlock(identifier, lineSet));
                }
            }
        }
        return staticBlockSet;
    }

    Set<ModifiedStaticBlock> matchModifiedStaticBlocksAndLines(Map<String, int[]> modifiedStaticBlocks, List<ModifiedLine> modifiedLines) {
        def staticBlockSet = new HashSet<ModifiedStaticBlock>();

        if(modifiedStaticBlocks.entrySet().size() > 0 ) {
            for (def staticBlock : modifiedStaticBlocks.entrySet()) {
                String identifier = staticBlock.getKey();
                int[] lines = staticBlock.getValue();

                def lineSet = new HashSet<ModifiedLine>();

                for (def lineNumber : lines) {
                    def diffLine = findModifiedLineByNumber(lineNumber, modifiedLines);

                    if (diffLine != null) {
                        lineSet.add(diffLine);
                    }
                }
                staticBlockSet.add(new ModifiedStaticBlock(identifier, lineSet));
            }
        }

        return staticBlockSet;
    }
    public ModifiedLine findModifiedLineByText(String keyBlockedStatic , String blockedStatic, List<ModifiedLine> modifiedLines) {
        for (def modifiedLine: modifiedLines) {
            String line = modifiedLine.getContent().split("-")[0];
           // line = removerChar(line);
            //|| line.equals("static {")
            // if((!line.isEmpty() &&  Pattern.compile(line,Pattern.LITERAL).matcher(blockedStatic).find()) || line.equals("static {")){
            //  if (Pattern.compile(modifiedLine.getContent(),Pattern.LITERAL).matcher(blockedStatic).find()) {
            boolean temp = line.toString().trim().contains(blockedStatic);
            boolean  temp1 = Pattern.compile(line,Pattern.LITERAL).matcher(blockedStatic).find();
            boolean  temp2 = line.toString().trim().containsIgnoreCase(blockedStatic);
            boolean  temp3 = Pattern.compile(modifiedLine.getContent(),Pattern.LITERAL).matcher(blockedStatic).find();

            if((!line.isEmpty() && temp1 ) || line.equals("static {")){
                if(verifyIdentifierNumber(keyBlockedStatic, modifiedLine.getNumber()))
                   return modifiedLine;
            }
        }
        return null;
    }

    private String removerChar(String str){
        str = str.replace("/**", "");
        str = str.replace("*", "");
        str = str.replace("*/", "");
        return  str;
    }
    public ModifiedLine findModifiedLineByNumber(int lineNumber, List<ModifiedLine> modifiedLines) {
        for (def modifiedLine: modifiedLines) {
            if (modifiedLine.getNumber() == lineNumber) {
                return modifiedLine;
            }
        }
        return null;
    }
    public boolean verifyIdentifierNumber(String keyLineNumber, int lineNumber){
        if(getBeginIdentifier(keyLineNumber) <= lineNumber && getEndIdentifier(keyLineNumber) >= lineNumber){
           return true;
        }
        return false
    }
    public int getIdentifierNumber(String keyLineNumber){
        def number = Integer.parseInt(keyLineNumber.split(";|;\\s")[0]);
        return number;
    }
    public int getBeginIdentifier(String keyLineNumber){
         keyLineNumber = keyLineNumber.split(";|;\\s")[1];
        return Integer.parseInt(keyLineNumber.split("-")[0]);
    }
    public int getEndIdentifier(String keyLineNumber){
        keyLineNumber = keyLineNumber.split(";|;\\s")[1];
        return Integer.parseInt(keyLineNumber.split("-")[1]);
    }
    public boolean findModifiedLineByNumber(String lineNumberBeginEnd, List<ModifiedLine> modifiedLines) {
        for (def modifiedLine: modifiedLines) {
            if (modifiedLine.getNumber() >= Integer.valueOf(lineNumberBeginEnd )) {
                return true;
            }
        }
        return false;
    }

}