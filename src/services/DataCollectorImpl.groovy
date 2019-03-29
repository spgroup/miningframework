package services
import main.interfaces.DataCollector

import java.util.regex.Pattern
import java.util.regex.Matcher

import static main.script.MiningFramework.arguments
import main.util.*

class DataCollectorImpl extends DataCollector {

    public static enum Modification {ADDED, REMOVED, CHANGED}
    private File resultsFileLinks

    @Override
    public void collectData() {
        resultsFile = new File("${outputPath}/data/results.csv")
        if(arguments.isPushCommandActive()) {
            resultsFileLinks = new File("${outputPath}/data/results-links.csv")
        }

        getMutuallyModifiedMethods()
        println "Data collection finished!"
    }

    private void getMutuallyModifiedMethods() {
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())
        Set<String> mutuallyModifiedFiles = new HashSet<String>(leftModifiedFiles)
        mutuallyModifiedFiles.retainAll(rightModifiedFiles)

        for(file in mutuallyModifiedFiles) {
            Set<ModifiedMethod> leftModifiedMethods = getModifiedMethods(file, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA())
            Set<ModifiedMethod> rightModifiedMethods = getModifiedMethods(file, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA())
            def mutuallyModifiedMethods = getMethodsIntersection(leftModifiedMethods, rightModifiedMethods)
            Set<ModifiedMethod> mergeModifiedMethods = getModifiedMethods(file, mergeCommit.getAncestorSHA(), mergeCommit.getSHA())
            
            if (mutuallyModifiedMethods.size() > 0) {
                String className = getClassName(file, mergeCommit.getAncestorSHA())
                for(method in mergeModifiedMethods) 
                    analyseModifiedMethods(className, mutuallyModifiedMethods, method, file)

                assembleResults(className.replaceAll('\\.', '\\/'), file)
            }
        }
    }

    private void assembleResults(String classPath, String file) {
        String path = "${outputPath}/files/${project.getName()}/${mergeCommit.getSHA()}/${classPath}/"
        File results = new File(path)
        if(!results.exists())
            results.mkdirs()

        FileManager.copyAndMoveFile(project, file, mergeCommit.getLeftSHA(), "${path}/left.java")
        FileManager.copyAndMoveFile(project, file, mergeCommit.getRightSHA(), "${path}/right.java")
        FileManager.copyAndMoveFile(project, file, mergeCommit.getAncestorSHA(), "${path}/base.java")
        FileManager.copyAndMoveFile(project, file, mergeCommit.getSHA(), "${path}/merge.java")
    }
    
    private void analyseModifiedMethods(String className, Map<String, ModifiedMethod[]> parentsModifiedMethods, ModifiedMethod mergeModifiedMethod, String file) {

        ModifiedMethod[] mutuallyModifiedMethods = parentsModifiedMethods[mergeModifiedMethod.getSignature()]
        if (mutuallyModifiedMethods != null) {
            Set<Integer> leftAddedLines = new HashSet<Integer>()
            Set<Tuple2> leftDeletedLines = new HashSet<Tuple2>()
            Set<Integer> rightAddedLines = new HashSet<Integer>()
            Set<Tuple2> rightDeletedLines = new HashSet<Tuple2>()

            // mutuallyModifiedMethods[0] = left's methods; mutuallyModifiedMethods[1] = right's methods;
            for(line in mergeModifiedMethod.getModifiedLines()) {
                if(containsLine(mutuallyModifiedMethods[0], line))
                    checkAndAddLine(line, leftAddedLines, leftDeletedLines)

                if(containsLine(mutuallyModifiedMethods[1], line)) 
                    checkAndAddLine(line, rightAddedLines, rightDeletedLines)
                    
            }
            printResults(className, mergeModifiedMethod.getSignature(), leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines)
        }
    }
  
    private boolean containsLine(ModifiedMethod method, ModifiedLine line) {
        for(lineit in method.getModifiedLines())
            if(lineit.equals(line))
                return true
        return false
    }

    private void printResults(String className, String method, Set<Integer> leftAddedLines, Set<Integer> leftDeletedLines, Set<Integer> rightAddedLines, Set<Integer> rightDeletedLines) {
        resultsFile << "${project.getName()};${mergeCommit.getSHA()};${className};${method};${leftAddedLines};${leftDeletedLines};${rightAddedLines};${rightDeletedLines}\n"
    
        // Add links.
        if(arguments.isPushCommandActive())
            addLinks(arguments.getRemoteRepositoryURL())
    }

    private void addLinks(String remoteRepositoryURL) {
        String projectLink = addLink(remoteRepositoryURL, project.getName())
        String mergeCommitSHALink = addLink(remoteRepositoryURL, "${project.getName()}/files/${project.getName()}/${mergeCommit.getSHA()}")
        String classNameLink = addLink(remoteRepositoryURL, "${project.getName()}/files/${project.getName()}/${mergeCommit.getSHA()}/${className.replaceAll('\\.', '\\/')}")
        
        resultsFileLinks << "${projectLink}&${mergeCommitSHALink}&${classNameLink}&${method}&${leftAddedLines}&${leftDeletedLines}&${rightAddedLines}&${rightDeletedLines}\n"
    }
   
    private String addLink(String url, String path) {
        return "=HYPERLINK(${url}/tree/master/output-${path};${path})"
    }
  
    private void checkAndAddLine(ModifiedLine line, Set<Integer> addedLines,  Set<Tuple2> deletedLines) {
        if (line.getType() == Modification.ADDED || line.getType() == Modification.CHANGED)
            addedLines.add(line.getNumber())
        else 
            deletedLines.add(line.getDeletedLineNumbersTuple())
    }

    /*
        DiffJ's output for methods' modification is the following:
        <rangei>,<rangef>: code (changed | added | removed) in <methodname>

        - rangei is a range of lines affected from the 'initial' file.
        - rangef is a range of lines affected from the 'final' file.
        - changed is reported when a removal and addition happen to the same line.
        
        After that, Diffj outputs three formats of line:
        < content 
        < content1 } removed lines (size of rangei).
        < ...     
        --- -> separator
        > content 
        > content1 } added lines (size of rangef).
        > ...  
        
        This algorithm detects such lines, associating them with their correspondent modifications.
        Also, it counts the rangef to check lines number.
    */
    private Set<ModifiedMethod> getModifiedMethods(String filePath, String ancestorSHA, String commitSHA) {
        Set<ModifiedMethod> modifiedMethods = new HashSet<ModifiedMethod>()
        File ancestorFile = FileManager.copyFile(project, filePath, ancestorSHA) 
        File mergeFile = FileManager.copyFile(project, filePath, commitSHA)

        Process diffJ = ProcessRunner.runProcess('dependencies', 'java', '-jar', 'diffj.jar', ancestorFile.getAbsolutePath(), mergeFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(diffJ.getInputStream()))
        String[] output = reader.readLines() 

        def methodAnalysisExpression = ~/.+ code (changed|added|removed) in .+/
        for(int i = 0; i < output.length; i++) {
            String line = output[i]

            if(line ==~ methodAnalysisExpression) {
                String signature = getSignature(line)
                
                String lineNumbers = line.substring(0, line.indexOf('code') - 1)
                List<Integer> removedLineNumbers = getRemovedLineNumbers(lineNumbers)
                List<Integer> addedLineNumbers = getAddedLineNumbers(lineNumbers)

                Modification modificationType = getModificationType(line) // Changed, added or removed.
              
                insertMethod(modifiedMethods, signature, getModifiedLines(removedLineNumbers, addedLineNumbers, modificationType, output, i + 1))
            }
                
        }

        FileManager.delete(ancestorFile)
        FileManager.delete(mergeFile)
        return modifiedMethods
    
    }

    private String getSignature(String line) {
        return line.substring(line.indexOf(" in ") + 4)
    }

    private List<Integer> getRemovedLineNumbers(String lineNumbers) {
        for (int i = 0; i < lineNumbers.size(); i++) {
            if(lineNumbers[i] == 'c' || lineNumbers[i] == 'd' || lineNumbers[i] == 'a')
                return parseLines(lineNumbers.substring(0, i))
        }
    }

    private List<Integer> getAddedLineNumbers(String lineNumbers) {
        for (int i = 0; i < lineNumbers.size(); i++) {
            if(lineNumbers[i] == 'c' || lineNumbers[i] == 'd' || lineNumbers[i] == 'a')
                return parseLines(lineNumbers.substring(i + 1))
        }
    }

    private Modification getModificationType(String line) {
        if(line.contains('changed'))
            return Modification.CHANGED
        else if(line.contains('added'))
            return Modification.ADDED
        else
            return Modification.REMOVED
    }

    private Set<ModifiedLine> getModifiedLines(List<Integer> removedLineNumbers, List<Integer> addedLineNumbers, Modification type, String[] outputLines, int start) {
        if(type == Modification.REMOVED)
            return getDeletedLinesTuple(removedLineNumbers, addedLineNumbers, type, outputLines, start)
        else if(type == Modification.ADDED)
            return getAddedLines(addedLineNumbers, type, outputLines, start)
        else 
            return getChangedLines(addedLineNumbers, type, outputLines, start)
    }

    private Set<ModifiedLine> getDeletedLinesTuple(List<Integer> removedLineNumbers, List<Integer> addedLineNumbers, Modification type, String[] outputLines, int iterator) {
        Set<ModifiedLine> modifiedLines = new HashSet<ModifiedLine>()
        for(int i = 0; outputLines[iterator].startsWith('<'); i++) {
            String content = outputLines[iterator].substring(1)
            ModifiedLine modifiedLine = new ModifiedLine(content, new Tuple2(removedLineNumbers[i], addedLineNumbers[0]), type)
            modifiedLines.add(modifiedLine)
            iterator++
        }
        return modifiedLines
    }

    private Set<ModifiedLine> getAddedLines(List<Integer> addedLineNumbers, Modification type, String[] outputLines, int iterator) {
        Set<ModifiedLine> modifiedLines = new HashSet<ModifiedLine>()
        for(int i = 0; isLineModification(outputLines, iterator); iterator++) {
            if(outputLines[iterator].startsWith('>')) {
                String content = outputLines[iterator].substring(1)
                ModifiedLine modifiedLine = new ModifiedLine(content, addedLineNumbers[i], type)
                modifiedLines.add(modifiedLine)
                i++
            }
        }
        return modifiedLines
    }

    private Set<ModifiedLine> getChangedLines(List<Integer> addedLineNumbers, Modification type, String[] outputLines, int iterator) {
        Set<ModifiedLine> modifiedLines = new HashSet<ModifiedLine>()
        for(int i = 0; isLineModification(outputLines, iterator); iterator++) {
            if(!outputLines[i].startsWith('---')) {
                String content = outputLines[iterator].substring(1)
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

    private ArrayList<Integer> parseLines(String lines) {
        List<Integer> modifiedLines = new ArrayList<Integer>()
        
        int commaIndex = lines.indexOf(',')
        if (commaIndex == -1) 
            modifiedLines.add(Integer.parseInt(lines))
        else {
            int start = Integer.parseInt(lines.substring(0, commaIndex))
            int end = Integer.parseInt(lines.substring(commaIndex + 1))
            for (int i = start; i <= end; i++)
                modifiedLines.add(i)
        }

        return modifiedLines
    }

    private Map<String, ModifiedMethod[]> getMethodsIntersection(Set<ModifiedMethod> leftMethods, Set<ModifiedMethod> rightMethods) {
        Map<String, ModifiedMethod[]> intersection = [:]
        for(leftMethod in leftMethods) {
            for(rightMethod in rightMethods) 
                if(leftMethod.equals(rightMethod))
                    intersection.put(leftMethod.getSignature(), [leftMethod, rightMethod])
        }
        return intersection
    }

    private String getClassName(String file, String SHA) {
        String className
        String classPackage = ""

        Pattern pattern = Pattern.compile("/?([A-Z][A-Za-z0-9]*?)\\.java")
        Matcher matcher = pattern.matcher(file)
        if(matcher.find()) 
            className = matcher.group(1)

        Process gitCatFile = ProcessRunner.runProcess(project.getPath(), 'git', 'cat-file', '-p', "${SHA}:${file}")
        gitCatFile.getInputStream().eachLine {
            String lineNoWhitespace = it.replaceAll("\\s", "")
            if(lineNoWhitespace.take(7).equals('package')) {
                classPackage = lineNoWhitespace.substring(7, lineNoWhitespace.indexOf(';')) 
            }
        }

        return (classPackage.equals("") ? "" : classPackage + '.') + className
    }

    
}