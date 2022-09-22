package services.dataCollectors.staticBlockCollector

import org.apache.commons.lang3.StringUtils
import services.dataCollectors.modifiedLinesCollector.ModifiedLine

import java.util.regex.Pattern;

public class StaticBlockModifiedLinesMatcher {
    private String CONST_INITIALIZER_DECLARATION = "static-";
    Set<StaticBlock> matchModifiedStaticBlocksASTLines(Map<String, String> collectionsStaticBlocksAncestor, Map<String, String> collectionsStaticBlocks, List<ModifiedLine> modifiedLines, String filePath) {
        def staticBlockSet = new HashSet<StaticBlock>();
        /*
        * Qunando há diferença entre os arquivos contendo blocos, com isso já caracteriza que houve alteração
        */
        if(collectionsStaticBlocks.entrySet()?.size() != collectionsStaticBlocksAncestor.entrySet()?.size()){
            for (def initializerDeclaration : collectionsStaticBlocks.entrySet()) {
                String identifier = initializerDeclaration.getKey();
                String blockedStatic = initializerDeclaration.getValue();
                identifier = getIdentifierNumber(identifier);
                def lineSet = new HashSet<ModifiedLine>();
                staticBlockSet.add(new StaticBlock(identifier, lineSet,filePath));

              /*  def diffLineText = findModifiedLineByText(identifier,blockedStatic, modifiedLines);
                identifier = getIdentifierNumber(identifier);
                if (diffLineText != null) {
                    lineSet.add(diffLineText);
                    staticBlockSet.add(new StaticBlock(identifier, lineSet,filePath));
                }*/
            }
            return staticBlockSet;
        }
        if (collectionsStaticBlocks.entrySet()?.size() > 0) {
            for (def initializerDeclaration : collectionsStaticBlocks.entrySet()) {
                String identifier = initializerDeclaration.getKey();
                //String identifier = CONST_INITIALIZER_DECLARATION + initializerDeclaration.getKey();
                String blockedStatic = initializerDeclaration.getValue();

                def lineSet = new HashSet<ModifiedLine>();
                def diffLineText = findModifiedLineByText(identifier,blockedStatic, modifiedLines);
                identifier = getIdentifierNumber(identifier);
                if (diffLineText != null) {
                    lineSet.add(diffLineText);
                    staticBlockSet.add(new StaticBlock(identifier, lineSet,filePath));
                }
            }
        }
        if (collectionsStaticBlocksAncestor.entrySet()?.size() > 0) {
            for (def initializerDeclaration : collectionsStaticBlocksAncestor.entrySet()) {
                String identifier = initializerDeclaration.getKey();
                //String identifier = CONST_INITIALIZER_DECLARATION + initializerDeclaration.getKey();
                String blockedStatic = initializerDeclaration.getValue();

                def lineSet = new HashSet<ModifiedLine>();
                def diffLineText = findModifiedLineByText(identifier,blockedStatic, modifiedLines);
                identifier = getIdentifierNumber(identifier);
                if (diffLineText != null) {
                    lineSet.add(diffLineText);
                    staticBlockSet.add(new StaticBlock(identifier, lineSet,filePath));
                }
            }
        }
        return staticBlockSet;
    }

    Set<StaticBlock> matchModifiedStaticBlocksAndLines(Map<String, int[]> modifiedStaticBlocks, List<ModifiedLine> modifiedLines) {
        def staticBlockSet = new HashSet<StaticBlock>();

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
                staticBlockSet.add(new StaticBlock(identifier, lineSet));
            }
        }

        return staticBlockSet;
    }
    public ModifiedLine findModifiedLineByText(String keyBlockedStatic , String blockedStatic, List<ModifiedLine> modifiedLines) {
        for (def modifiedLine: modifiedLines) {
            String line = modifiedLine.getContent().split("-")[0];
            String lineTemp = getStringContentIntoSingleLineNoSpacing(line)
            String bStatic = getStringContentIntoSingleLineNoSpacing(StringUtils.removeEnd(StringUtils.removeStart(blockedStatic,"{"),"}"))

            boolean  temp4 = lineTemp.equals(bStatic)

            if((!line.isEmpty() && temp4 ) || line.equals("static {")){
                if(verifyIdentifierNumber(keyBlockedStatic, modifiedLine.getNumber()))
                   return modifiedLine;
            }
        }
        return null;
    }
    private static String getStringContentIntoSingleLineNoSpacing(String content) {
        return (content.replaceAll("\\r\\n|\\r|\\n|\\u0000","")).replaceAll("\\s+","");
    }
    private static String getRemovedKeysOfBlockStatic(String content){
        List<String> list = content.toList()
        if(list.size() > 0){
            if(list.first().equals("{") && list.last().equals("}")){
                 list.removeLast()
                 list.remove(0)
            }
        }
        String cont = list.toString().replace("[","").replace("]","").replaceAll(",","\r\n")
        String temp = list.stream().map( e -> e.toString() ).collect( toList() )
        return cont
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