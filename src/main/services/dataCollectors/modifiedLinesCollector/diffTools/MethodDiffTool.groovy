package services.dataCollectors.modifiedLinesCollector.diffTools

import services.dataCollectors.modifiedLinesCollector.ModifiedMethod

/**
 * A tool that tells which methods (and fields and initializer blocks) changed from one version of a Java file to
 * another. ModifiedMethodsHelper combines it with the textual diff, which gives the content and the type of each
 * modified line. To plug in another tool, implement this interface and register it in MethodDiffTools.
 */
interface MethodDiffTool {

    /**
     * @return for each changed method, field or initializer block of the target (e.g. "m(String, int)", "TABLE",
     * "static block"), the lines where it changed, numbered as the textual diff (diff) numbers them: lines of the
     * target, and for removed lines the line of the target after which they were removed
     */
    Map<String, int[]> getModifiedMethodsLines(File ancestorFile, File targetFile)

    /**
     * @return the methods changed or added, with the class and the return type in the signature
     * (e.g. "<a.C: void m(int)>"), without their lines
     */
    List<ModifiedMethod> getAllModifiedMethods(File ancestorFile, File targetFile)
}
