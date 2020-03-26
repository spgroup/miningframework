package services

@Grab(group='org.apache.commons', module='commons-lang3', version='3.9')
import main.interfaces.DataCollector
import main.project.MergeCommit
import main.project.Project
import main.util.FileManager
import main.util.ProcessRunner
import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

import static main.app.MiningFramework.arguments

class ExperimentalDataCollectorImpl implements DataCollector {

    public enum ModificationType {
        Added,
        Changed,
        Removed
    }

    private File resultsFileLinks

    private final def methodOrAttributeModification = ~/.+ code (changed|added|removed) in .+/

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        setUp()

        collectMutuallyModifiedMethodsAndAttributes(project, mergeCommit)
        println "${project.getName()} - Data collection finished!"
    }

    private void setUp() {
        String outputPath = arguments.getOutputPath()
        File resultsFile = createFilesIfTheyDontExist(outputPath)

        if(arguments.isPushCommandActive()) {
            resultsFileLinks = new File("${outputPath}/data/results-links.csv")
        }
    }

    private File createFilesIfTheyDontExist(String outputPath) {
        File experimentalDataDir = new File(outputPath + '/data')
        if (!experimentalDataDir.exists()) {
            experimentalDataDir.mkdirs()
        }

        File experimentalDataFile = new File(outputPath + "/data/results.csv")
        if (!experimentalDataFile.exists()) {
            experimentalDataFile << addHeaderLinesForOutputFile()
        }

        return experimentalDataFile
    }

    protected String addHeaderLinesForOutputFile(){
        return 'project;merge commit;className;method;left modifications;left deletions;right modifications;right deletions\n'
    }

    private void collectMutuallyModifiedMethodsAndAttributes(Project project, MergeCommit mergeCommit) {
        Set<String> mutuallyModifiedFilePaths = collectMutuallyModifiedFiles(project, mergeCommit)

        for(String filePath in mutuallyModifiedFilePaths) {
            Set<ModifiedDeclaration> allModifiedMethodsAndAttributes = getModifiedMethodsAndAttributes(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getSHA())
            Map<String, Tuple2<ModifiedDeclaration, ModifiedDeclaration>> mutuallyModifiedMethodsAndAttributes = getMutuallyModifiedMethodsAndAttributes(project, mergeCommit, filePath)

            if(!mutuallyModifiedMethodsAndAttributes.isEmpty()) {
                String className = getClassFullyQualifiedName(project, filePath, mergeCommit.getAncestorSHA())

                for(declaration in allModifiedMethodsAndAttributes) {
                    storeModifiedAttributesAndMethods(project, mergeCommit, className, mutuallyModifiedMethodsAndAttributes, declaration, filePath)
                }

                saveMergeScenarioFiles(project, mergeCommit, className.replaceAll('\\.', '\\/'), filePath)
            }
        }
    }

    private void saveMergeScenarioFiles(Project project, MergeCommit mergeCommit, String classFilePath, String filePath) {
        String outputPath = arguments.getOutputPath()
        
        String path = "${outputPath}/files/${project.getName()}/${mergeCommit.getSHA()}/transformed/source/"
        File results = new File(path)
        if(!results.exists())
            results.mkdirs()

        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getLeftSHA(), "${path}/left.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getRightSHA(), "${path}/right.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getAncestorSHA(), "${path}/base.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getSHA(), "${path}/merge.java")
    }

    private void storeModifiedAttributesAndMethods(Project project, MergeCommit mergeCommit, String className,
                                           Map<String, Tuple2<ModifiedDeclaration, ModifiedDeclaration>> mutuallyModifiedMethodsAndAttributesMap,
                                           ModifiedDeclaration mergeModifiedDeclaration, String filePath) {

        Tuple2<ModifiedDeclaration, ModifiedDeclaration> mutuallyModifiedDeclarations = mutuallyModifiedMethodsAndAttributesMap[mergeModifiedDeclaration.getSignature()]
        if(mutuallyModifiedDeclarations != null) {
            Set<Integer> leftAddedLines = new HashSet<Integer>()
            Set<Tuple2> leftDeletedLines = new HashSet<Tuple2>()
            Set<Integer> rightAddedLines = new HashSet<Integer>()
            Set<Tuple2> rightDeletedLines = new HashSet<Tuple2>()

            for(modifiedLine in mergeModifiedDeclaration.getModifiedLines()) {
                if(containsLine(mutuallyModifiedDeclarations.getFirst(), modifiedLine))
                    addLine(modifiedLine, leftAddedLines, leftDeletedLines)

                if(containsLine(mutuallyModifiedDeclarations.getSecond(), modifiedLine))
                    addLine(modifiedLine, rightAddedLines, rightDeletedLines)
            }

            printResults(project, mergeCommit, className, mergeModifiedDeclaration.getSignature(), leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines)
        }
    }

    private synchronized void printResults(Project project, MergeCommit mergeCommit, String className, String modifiedDeclarationSignature,
                      HashSet<Integer> leftAddedLines, HashSet<Tuple2> leftDeletedLines, HashSet<Integer> rightAddedLines,
                      HashSet<Tuple2> rightDeletedLines) {

        File resultsFile = new File("${arguments.getOutputPath()}/data/results.csv")

        resultsFile << addMergeCommitInfoIntoOutputFile(project, mergeCommit, className, modifiedDeclarationSignature, leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines)

        // Add links.
        if(arguments.isPushCommandActive())
            addLinks(project.getName(), mergeCommit.getSHA(), className, modifiedDeclarationSignature, leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines, arguments.getResultsRemoteRepositoryURL())

    }

    protected String addMergeCommitInfoIntoOutputFile(Project project, MergeCommit mergeCommit, String className, String modifiedDeclarationSignature,
                      HashSet<Integer> leftAddedLines, HashSet<Tuple2> leftDeletedLines, HashSet<Integer> rightAddedLines,
                      HashSet<Tuple2> rightDeletedLines){
        return "${project.getName()};${mergeCommit.getSHA()};${className};${modifiedDeclarationSignature};${leftAddedLines};${leftDeletedLines};${rightAddedLines};${rightDeletedLines}\n"
    }

    void addLinks(String projectName, String mergeCommitSHA, String className, String modifiedDeclarationSignature,
                  HashSet<Integer> leftAddedLines, HashSet<Tuple2> leftDeletedLines, HashSet<Integer> rightAddedLines,
                  HashSet<Tuple2> rightDeletedLines, String remoteRepositoryURL) {
        String projectLink = addLink(remoteRepositoryURL, projectName)
        String mergeCommitSHALink = addLink(remoteRepositoryURL, "${projectName}/files/${projectName}/${mergeCommitSHA}")
        String classNameLink = addLink(remoteRepositoryURL, "${projectName}/files/${projectName}/${mergeCommitSHA}/${className.replaceAll('\\.', '\\/')}")

        resultsFileLinks << "${projectLink}&${mergeCommitSHALink}&${classNameLink}&${modifiedDeclarationSignature}&${leftAddedLines}&${leftDeletedLines}&${rightAddedLines}&${rightDeletedLines}\n"
    }

    private String addLink(String url, String path) {
        return "=HYPERLINK(${url}/tree/master/output-${path};${path})"
    }

    private void addLine(ModifiedLine modifiedLine, HashSet<Integer> addedLines, Set<Tuple2> deletedLines) {
        if (modifiedLine.getType() == ModificationType.Added || modifiedLine.getType() == ModificationType.Changed)
            addedLines.add(modifiedLine.getNumber())
        else // if it was removed, there's a tuple.
            deletedLines.add(modifiedLine.getDeletedLineNumbersTuple())
    }

    private boolean containsLine(ModifiedDeclaration declaration, ModifiedLine modifiedLine) {
        for(lineIterator in declaration.getModifiedLines())
            if(lineIterator == modifiedLine)
                return true
        return false
    }

    private String getClassFullyQualifiedName(Project project, String filePath, String SHA) {
        String className = getClassName(filePath)
        String classPackage = getClassPackage(project, SHA, filePath)

        return (classPackage == "" ? "" : classPackage + '.') + className
    }

    private String getClassPackage(Project project, String SHA, String filePath) {
        Process gitCatFile = ProcessRunner.runProcess(project.getPath(), 'git', 'cat-file', '-p', "${SHA}:${filePath}")
        
        def fileLines = gitCatFile.getInputStream().readLines()

        for (String fileLine : fileLines) {
            String lineNoWhitespace = StringUtils.deleteWhitespace(fileLine)
            if(lineNoWhitespace.take(7) == 'package') {
                return lineNoWhitespace.substring(7, lineNoWhitespace.indexOf(';')) // assuming the ; will be at the same line
            }
        }
        
        return "";
    }

    private String getClassName(String filePath) {
        Pattern pattern = Pattern.compile("/?([A-Z][A-Za-z0-9]*?)\\.java") // find the name of the class by the name of the file
        Matcher matcher = pattern.matcher(filePath)
        if(matcher.find())
            return matcher.group(1)
    }

    private Map<String, Tuple2<ModifiedDeclaration, ModifiedDeclaration>> getMutuallyModifiedMethodsAndAttributes(Project project, MergeCommit mergeCommit, String filePath) {
        Set<ModifiedDeclaration> leftModifiedDeclarations = getModifiedMethodsAndAttributes(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA())
        Set<ModifiedDeclaration> rightModifiedDeclarations = getModifiedMethodsAndAttributes(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA())
        return intersectAndBuildMap(leftModifiedDeclarations, rightModifiedDeclarations)
    }

    Map<String, Tuple2<ModifiedDeclaration, ModifiedDeclaration>> intersectAndBuildMap(Set<ModifiedDeclaration> leftModifiedDeclarations, Set<ModifiedDeclaration> rightModifiedDeclarations) {
        Map<String, Tuple2<ModifiedDeclaration, ModifiedDeclaration>> intersection = [:]

        for(leftDeclaration in leftModifiedDeclarations) {
            for(rightDeclaration in rightModifiedDeclarations) {
                if(leftDeclaration == rightDeclaration)
                    intersection.put(leftDeclaration.getSignature(), new Tuple2(leftDeclaration, rightDeclaration))
            }
        }

        return intersection
    }

    private Set<ModifiedDeclaration> getModifiedMethodsAndAttributes(Project project, String filePath, String ancestorSHA, String targetSHA) {
        File ancestorFile = FileManager.getFileInCommit(project, filePath, ancestorSHA)
        File targetFile = FileManager.getFileInCommit(project, filePath, targetSHA)

        String[] diffResultLines = runDiffJ(ancestorFile, targetFile)
        return getModifiedMethodsAndAttributes(diffResultLines)
    }

    private Set<ModifiedDeclaration> getModifiedMethodsAndAttributes(String[] diffResultLines) {
        Set<ModifiedDeclaration> modifiedMethodsAndAttributes = new HashSet<String>()

        for (int i = 0; i < diffResultLines.length; i++) {
            String line = diffResultLines[i]

            if(line ==~ methodOrAttributeModification) {
                if (line !=~ /.+ static block .+/) {
                    insertDeclaration(modifiedMethodsAndAttributes, parseLine(line, diffResultLines, i + 1))
                }
            }
        }

        return modifiedMethodsAndAttributes
    }

    private void insertDeclaration(Set<ModifiedDeclaration> modifiedDeclarations, ModifiedDeclaration modifiedDeclaration) {
        for(declarationIterator in modifiedDeclarations) {
            if(declarationIterator.getSignature() == modifiedDeclaration.getSignature()) {
                declarationIterator.addAllLines(modifiedDeclaration.getModifiedLines())
                return
            }
        }
        modifiedDeclarations.add(modifiedDeclaration)
    }

    private ModifiedDeclaration parseLine(String line, String[] diffResultLines, int start) {
        String identifier = getIdentifier(line) // signature for methods, name for attributes

        String modifiedLinesRange = getModifiedLinesRange(line)
        List<Integer> removedLineNumbers = getRemovedLineNumbers(modifiedLinesRange)
        List<Integer> addedLineNumbers = getAddedLineNumbers(modifiedLinesRange)

        ModificationType modificationType = getModificationType(line)

        Set<ModifiedLine> modifiedLines = getModifiedLines(modificationType, diffResultLines, removedLineNumbers, addedLineNumbers, start)
        return new ModifiedDeclaration(identifier, modifiedLines)
    }

    private List<Integer> getRemovedLineNumbers(String modifiedLinesRange) {
        for (int i = 0; i < modifiedLinesRange.size(); i++) {
            if(modifiedLinesRange[i] == 'c' || modifiedLinesRange[i] == 'd' || modifiedLinesRange[i] == 'a')
                return parseLines(modifiedLinesRange.substring(0, i))
        }
        return null
    }

    private List<Integer> getAddedLineNumbers(String modifiedLinesRange) {
        for (int i = 0; i < modifiedLinesRange.size(); i++) {
            if(modifiedLinesRange[i] == 'c' || modifiedLinesRange[i] == 'd' || modifiedLinesRange[i] == 'a')
                return parseLines(modifiedLinesRange.substring(i + 1))
        }
        return null
    }

    private ArrayList<Integer> parseLines(String lines) {
        List<Integer> modifiedLines = new ArrayList<Integer>()

        // the lines are presented as deletedStart-deletedEnd,addedStart-addedEnd

        int commaIndex = lines.indexOf(',')
        if (commaIndex == -1) // if there's not a comma, it's a single line number
            modifiedLines.add(Integer.parseInt(lines))
        else { // otherwise, it's a range of line numbers n-m
            int start = Integer.parseInt(lines.substring(0, commaIndex))
            int end = Integer.parseInt(lines.substring(commaIndex + 1))
            for (int i = start; i <= end; i++)
                modifiedLines.add(i)
        }

        return modifiedLines
    }

    private Set<ModifiedLine> getModifiedLines(ModificationType type, String[] outputLines, List<Integer> removedLineNumbers, List<Integer> addedLineNumbers, int start) {
        if(type == ModificationType.Removed)
            return getDeletedLinesTuple(removedLineNumbers, addedLineNumbers, type, outputLines, start)
        else if(type == ModificationType.Added)
            return getAddedLines(addedLineNumbers, type, outputLines, start)
        else
            return getChangedLines(addedLineNumbers, type, outputLines, start)
    }

    private Set<ModifiedLine> getDeletedLinesTuple(List<Integer> removedLineNumbers, List<Integer> addedLineNumbers, ModificationType type, String[] outputLines, int iterator) {
        Set<ModifiedLine> modifiedLines = new HashSet<ModifiedLine>()
        for(int i = 0; outputLines[iterator].startsWith('<'); i++) { // while it's a deletion line
            String content = outputLines[iterator].substring(1) // remove the symbol

            ModifiedLine modifiedLine = new ModifiedLine(content, new Tuple2(removedLineNumbers[i], addedLineNumbers[0]), type)
            modifiedLines.add(modifiedLine)
            iterator++
        }
        return modifiedLines
    }

    private Set<ModifiedLine> getAddedLines(List<Integer> addedLineNumbers, ModificationType type, String[] outputLines, int iterator) {
        Set<ModifiedLine> modifiedLines = new HashSet<ModifiedLine>()
        for(int i = 0; isLineModification(outputLines, iterator); iterator++) {
            if(outputLines[iterator].startsWith('>')) { // ignore deletion and neutral lines
                String content = outputLines[iterator].substring(1) // remove the symbol

                ModifiedLine modifiedLine = new ModifiedLine(content, addedLineNumbers[i], type)
                modifiedLines.add(modifiedLine)
                i++
            }
        }
        return modifiedLines
    }

    private Set<ModifiedLine> getChangedLines(List<Integer> addedLineNumbers, ModificationType type, String[] outputLines, int iterator) {
        Set<ModifiedLine> modifiedLines = new HashSet<ModifiedLine>()
        for(int i = 0; isLineModification(outputLines, iterator); iterator++) {
            if(!outputLines[i].startsWith('---')) { // ignore neutral lines
                String content = outputLines[iterator].substring(1) // remove the symbol

                ModifiedLine modifiedLine = new ModifiedLine(content, addedLineNumbers[i], type)
                modifiedLines.add(modifiedLine)
                if(outputLines[i].startsWith('>'))
                    i++
            }
        }
        return modifiedLines
    }

    private boolean isLineModification(String[] outputLines, int i) {
        return outputLines[i].startsWith('<') || outputLines[i].startsWith('---') || outputLines[i].startsWith('>')
    }

    private ModificationType getModificationType(String line) {
        if(line.contains('changed'))
            return ModificationType.Changed
        else if(line.contains('added'))
            return ModificationType.Added
        else
            return ModificationType.Removed
    }

    private String getModifiedLinesRange(String line) {
        return StringUtils.substringBefore(line, " code")
    }

    private String getIdentifier(String line) {
        return line.substring(line.indexOf(" in ") + 4)
    }

    private String[] runDiffJ(File ancestorFile, File targetFile) {
        Process diffJ = ProcessRunner.runProcess('dependencies', 'java', '-jar', 'diffj.jar', ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(diffJ.getInputStream()))
        String[] output = reader.readLines()
        reader.close()
        return output
    }

    private Set<String> collectMutuallyModifiedFiles(Project project, MergeCommit mergeCommit) {
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())

        return leftModifiedFiles.intersect(rightModifiedFiles)
    }
}
