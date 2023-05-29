package services.dataCollectors.staticBlockCollector

import org.apache.commons.lang3.StringUtils
import services.dataCollectors.modifiedLinesCollector.ModifiedLine
import util.FileManager

public class StaticBlockModifiedLinesMatcher {

    Set<StaticBlock> matchModifiedStaticBlocksAST(Map<String, String> collectionsStaticBlocksAncestor, Map<String, String> collectionsStaticBlocks, List<ModifiedLine> modifiedLines, String filePath) {
        def staticBlockSet = new HashSet<StaticBlock>();
        /*
        * When there is a difference between files containing blocks. It is already possible to characterize that there was a change.
        */
        if (collectionsStaticBlocks.entrySet().size() != collectionsStaticBlocksAncestor.entrySet().size()) {
            for (def initializerDeclaration : collectionsStaticBlocks.entrySet().size() == 0 ? collectionsStaticBlocksAncestor.entrySet() : collectionsStaticBlocks.entrySet()) {
                String identifier = initializerDeclaration.getKey();
                identifier = getIdentifierNumber(identifier);
                staticBlockSet.add(new StaticBlock(identifier, filePath));
            }
        } else {
            for (def initializerDeclaration : collectionsStaticBlocks.entrySet()) {
                for (def ancestralInitBlcok : collectionsStaticBlocksAncestor.entrySet()) {
                    if (diffContentInitializationBlock(initializerDeclaration.getValue(), ancestralInitBlcok.getValue())) {
                        String identifier = initializerDeclaration.getKey();
                        identifier = getIdentifierNumber(identifier);
                        staticBlockSet.add(new StaticBlock(identifier, filePath));
                    }

                }
            }
        }
        return staticBlockSet;
    }

    private boolean diffContentInitializationBlock(String targetBlock, String ascentralBlock){
        String bStatic = getStringContentIntoSingleLineNoSpacing(StringUtils.removeEnd(StringUtils.removeStart(targetBlock,"{"),"}"))
        String aStatic = getStringContentIntoSingleLineNoSpacing(StringUtils.removeEnd(StringUtils.removeStart(ascentralBlock,"{"),"}"))

        return !(bStatic.equals(aStatic));
    }

    private static String getStringContentIntoSingleLineNoSpacing(String content) {
        return (content.replaceAll("\\r\\n|\\r|\\n|\\u0000","")).replaceAll("\\s+","");
    }

    private int getIdentifierNumber(String keyLineNumber){
        def number = Integer.parseInt(keyLineNumber.split(";|;\\s")[0]);
        return number;
    }
}
