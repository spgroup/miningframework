import java.util.regex.Pattern
import java.util.regex.Matcher
import ModifiedLine

class DataCollectorImpl extends DataCollector {

    public static enum Modification {ADDED, REMOVED, CHANGED}

    public DataCollectorImpl() {
       resultsFile = new File("output/data/results.csv")
        if(resultsFile.exists())
            resultsFile.delete()
        resultsFile << 'project;merge commit;class;method;left modifications;right modifications\n'
    }

    @Override
    public void collectData() {
        getMutuallyModifiedMethods()
        println "Data collection finished!"
    }

    private void getMutuallyModifiedMethods() {
        Set<String> leftModifiedFiles = getModifiedFiles(mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = getModifiedFiles(mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())
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
                    analyseModifiedMethods(className, mutuallyModifiedMethods, method)
            }
        }
    }
    
    private void analyseModifiedMethods(String className, Map<String, ModifiedMethod[]> parentsModifiedMethods, ModifiedMethod mergeModifiedMethod) {

        ModifiedMethod[] mutuallyModifiedMethods = parentsModifiedMethods[mergeModifiedMethod.getSignature()]
        if (mutuallyModifiedMethods != null) {
            Set<Integer> leftModifiedLines = new HashSet<Integer>()
            Set<Integer> rightModifiedLines = new HashSet<Integer>()

            for(line in mergeModifiedMethod.getModifiedLines()) {
                if(containsLine(mutuallyModifiedMethods[0], line))
                    leftModifiedLines.add(line.getNumber())
                if(containsLine(mutuallyModifiedMethods[1], line))
                    rightModifiedLines.add(line.getNumber())
            }
            printResults(className, mergeModifiedMethod.getSignature(), leftModifiedLines, rightModifiedLines)
        }
    }

    private boolean containsLine(ModifiedMethod method, ModifiedLine line) {
        for(lineit in method.getModifiedLines())
            if(lineit.equals(line))
                return true
        return false
    }

    private void printResults(String className, String method, Set<Integer> leftModifiedLines, Set<Integer> rightModifiedLines) {   
        resultsFile << "${project.getName()};${mergeCommit.getSHA()};${className};${method};${leftModifiedLines};${rightModifiedLines}\n"
    }

    private Set<String> getModifiedFiles(String childSHA, String ancestorSHA) {
        Set<String> modifiedFiles = new HashSet<String>()
        Process gitDiff = new ProcessBuilder('git', 'diff', '--name-only', childSHA, ancestorSHA)
            .directory(new File(project.getPath()))
            .start()
        
        gitDiff.getInputStream().eachLine {
            if(it.endsWith('.java'))
                modifiedFiles.add(it)
        }

        return modifiedFiles
    }

    private Set<ModifiedMethod> getModifiedMethods(String filePath, String ancestorSHA, String mergeCommitSHA) {
        Set<ModifiedMethod> modifiedMethods = new HashSet<ModifiedMethod>()

        File ancestorFile = copyFile(filePath, ancestorSHA) 
        File mergeFile = copyFile(filePath, mergeCommitSHA)

        Process diffJ = new ProcessBuilder('java', '-jar', 'diffj.jar', ancestorFile.getAbsolutePath(), mergeFile.getAbsolutePath())
            .directory(new File('dependencies'))
            .start()
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(diffJ.getInputStream())) 
        String line
        String signature
        Set<ModifiedLine> modifiedLines = new HashSet<ModifiedLine>()
        while((line = reader.readLine()) != null) {

            if(line.matches(".+ code (changed|added|removed) in .+")) {
                if(modifiedLines.size() > 0) {
                    insertMethod(modifiedMethods, signature, modifiedLines)
                    modifiedLines = new HashSet<ModifiedLine>()
                }
    
                int codeTokenIndex = line.indexOf("code")
                ArrayList<Integer> modifiedLinesNumber = getLineNumbers(line.substring(0, codeTokenIndex - 1))
                Modification modificationType = getModificationType(line.substring(codeTokenIndex + 2))
                signature = line.substring(line.indexOf(" in ") + 4)

                modifiedLines.addAll(getLines(modificationType, reader, modifiedLinesNumber))
            }
        }

        if(signature != null)
            insertMethod(modifiedMethods, signature, modifiedLines)

        ancestorFile.delete()
        mergeFile.delete()
        return modifiedMethods
    }

    private Set<ModifiedLine> getLines(Modification type, BufferedReader reader, modifiedLinesNumber) {
        Set<ModifiedLine> modifiedLines = new HashSet<ModifiedLine>()

        String line = reader.readLine()
        int i = 0
        while(line.startsWith('<') || line.startsWith('---') || line.startsWith('>')) {
            if((type == Modification.ADDED && line.startsWith('>')) 
            || (type == Modification.REMOVED && line.startsWith('<')) 
            || (type == Modification.CHANGED && (line.startsWith('<') || line.startsWith('>')))) {
                
                String content = line.substring(1)
                Modification lineType = line.startsWith('>') ? Modification.ADDED : Modification.REMOVED
                ModifiedLine modifiedLine = new ModifiedLine(content, modifiedLinesNumber[i], lineType)
                modifiedLines.add(modifiedLine)
                if(line.startsWith('>'))
                    i++
            }

            line = reader.readLine()
        }
        return modifiedLines
    }

    private void insertMethod(Set<ModifiedMethod> modifiedMethods, String signature, Set<ModifiedLine> modifiedLines) {
        for(method in modifiedMethods) 
            if(method.getSignature().equals(signature)) {
                method.addAll(modifiedLines)
                return
            }
        
        ModifiedMethod modifiedMethod = new ModifiedMethod(signature, modifiedLines)
        modifiedMethods.add(modifiedMethod)
    }

    private Modification getModificationType(String modification) {
        if(modification.contains('changed'))
            return Modification.CHANGED
        else if(modification.contains('added'))
            return Modification.ADDED
        else
            return Modification.REMOVED 
    }

    private ArrayList<Integer> getLineNumbers(String lineChanges) {
        for (int i = 0; i < lineChanges.size(); i++) {
            if(lineChanges[i] == 'c' || lineChanges[i] == 'd' || lineChanges[i] == 'a')
                return parseLines(lineChanges.substring(i + 1))
        }
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

    private File copyFile(String path, String SHA) {
        Process gitCatFile = new ProcessBuilder('git', 'cat-file', '-p', "${SHA}:${path}")
            .directory(new File(project.getPath()))
            .start()
    
        
        File target = new File("${SHA}.java")
        gitCatFile.getInputStream().eachLine {
            target << "${it}\n"
        }
       
        return target
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

        Process gitCatFile = new ProcessBuilder('git', 'cat-file', '-p', "${SHA}:${file}")
            .directory(new File(project.getPath()))
            .start()

        gitCatFile.getInputStream().eachLine {
            String lineNoWhitespace = it.replaceAll("\\s", "")
            if(lineNoWhitespace.take(7).equals('package')) {
                classPackage = lineNoWhitespace.substring(7, lineNoWhitespace.indexOf(';')) 
            }
        }

        return (classPackage.equals("") ? "" : classPackage + '.') + className
    }

    
}