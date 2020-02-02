package services.modifiedLinesCollector

import main.project.MergeCommit
import main.project.Project
import main.interfaces.DataCollector

import main.util.FileManager
import main.util.ProcessRunner

import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

import static main.app.MiningFramework.arguments

class ModifiedLinesCollector implements DataCollector {

    private File experimentalDataFile;
    private File experimentalDataFileWithLinks;

    private DiffParser textualDiffParser = new DiffParser();
    private ModifiedMethodsParser modifiedMethodsParser = new ModifiedMethodsParser();
    private MethodModifiedLinesMatcher modifiedMethodsMatcher = new MethodModifiedLinesMatcher();

    ModifiedLinesCollector() {
        // createOutputFiles(arguments.getOutputPath());
    }
    
    private void createOutputFiles(String outputPath) {
        File experimentalDataDir = new File(outputPath + '/data')
        
        if (!experimentalDataDir.exists()) {
            experimentalDataDir.mkdirs()
        }

        this.experimentalDataFile = new File(outputPath + "/data/results.csv")
        if (!experimentalDataFile.exists()) {
            this.experimentalDataFile << 'project;merge commit;className;method;left modifications;left deletions;right modifications;right deletions\n'
        }
        
       
        if(arguments.isPushCommandActive()) {
            this.experimentalDataFileWithLinks = new File("${outputPath}/data/result-links.csv");
        }
    }

    void collectData(Project project, MergeCommit mergeCommit) {
        createOutputFiles(arguments.getOutputPath())
        Set<String> mutuallyModifiedFiles = getMutuallyModifiedFiles(project, mergeCommit); 

        for (String filePath : mutuallyModifiedFiles) {
            Set<ModifiedMethod> allModifiedMethods = getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getSHA())
            Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> mutuallyModifiedMethods = getMutuallyModifiedMethods(project, mergeCommit, filePath);

            if (!mutuallyModifiedMethods.isEmpty()) {
                String className = getClassFullyQualifiedName(project, filePath, mergeCommit.getAncestorSHA())

                for (def method : allModifiedMethods) {
                    Tuple2<ModifiedMethod, ModifiedMethod> leftAndRightMethods = mutuallyModifiedMethods[method.getSignature()];
                    if (leftAndRightMethods != null) {
                        ModifiedMethod leftMethod = leftAndRightMethods.getFirst();
                        ModifiedMethod rightMethod = leftAndRightMethods.getSecond();

                        Set<Integer> leftAddedLines = new HashSet<Integer>();
                        Set<Integer> leftDeletedLines = new HashSet<Integer>();
                        Set<Integer> rightAddedLines = new HashSet<Integer>();
                        Set<Integer> rightDeletedLines = new HashSet<Integer>();

                        println method

                        for (def mergeLine : method.getLines()) {
                            if (leftMethod.getLines().contains(mergeLine)) {
                                if (mergeLine.getType() == ModifiedLine.ModificationType.Removed) {
                                    leftDeletedLines.add(mergeLine.getNumber());
                                } else {
                                    leftAddedLines.add(mergeLine.getNumber());
                                }
                            }
                            if (rightMethod.getLines().contains(mergeLine)) {
                                if (mergeLine.getType() == ModifiedLine.ModificationType.Removed) {
                                    rightDeletedLines.add(mergeLine.getNumber());
                                } else {
                                    rightAddedLines.add(mergeLine.getNumber());
                                }
                            }
                        }

                        saveMergeScenarioFiles(project, mergeCommit, className.replaceAll('\\.', '\\/'), filePath);
                        printResults(project, mergeCommit, className, method.getSignature(), leftAddedLines, leftDeletedLines, rightAddedLines,rightDeletedLines); 
                    }

                }

            }


        }
        println "${project.getName()} - ModifiedLinesCollector collection finished"
    }

    private void saveMergeScenarioFiles(Project project, MergeCommit mergeCommit, String classFilePath, String filePath) {
        String outputPath = arguments.getOutputPath()
        
        String path = "${outputPath}/files/${project.getName()}/${mergeCommit.getSHA()}/${classFilePath}/"
        File results = new File(path)
        if(!results.exists())
            results.mkdirs()

        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getLeftSHA(), "${path}/left.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getRightSHA(), "${path}/right.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getAncestorSHA(), "${path}/base.java")
        FileManager.copyAndMoveFile(project, filePath, mergeCommit.getSHA(), "${path}/merge.java")
    }

    private synchronized void printResults(Project project, MergeCommit mergeCommit, String className, String modifiedDeclarationSignature,
                      HashSet<Integer> leftAddedLines, HashSet<Integer> leftDeletedLines, HashSet<Integer> rightAddedLines,
                      HashSet<Integer> rightDeletedLines) {


        experimentalDataFile << "${project.getName()};${mergeCommit.getSHA()};${className};${modifiedDeclarationSignature};${leftAddedLines};${leftDeletedLines};${rightAddedLines};${rightDeletedLines}\n"

        // Add links.
        if(arguments.isPushCommandActive())
            addLinks(project.getName(), mergeCommit.getSHA(), className, modifiedDeclarationSignature, leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines, arguments.getResultsRemoteRepositoryURL())

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

    private Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> getMutuallyModifiedMethods(Project project, MergeCommit mergeCommit, String filePath) {
        Set<ModifiedMethod> leftModifiedMethods = getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA())
        Set<ModifiedMethod> rightModifiedMethods = getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA())
        return intersectAndBuildMap(leftModifiedMethods, rightModifiedMethods)
    }

    Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> intersectAndBuildMap(Set<ModifiedMethod> leftModifiedMethods, Set<ModifiedMethod> rightModifiedMethods) {
        Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> intersection = [:]

        for(leftMethod in leftModifiedMethods) {
            for(rightMethod in rightModifiedMethods) {
                if(leftMethod == rightMethod) {
                    intersection.put(leftMethod.getSignature(), new Tuple2(leftMethod, rightMethod))
                }
            }
        }

        return intersection
    }    
    private Set<String> getMutuallyModifiedFiles(Project project, MergeCommit mergeCommit) {
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())

        return leftModifiedFiles.intersect(rightModifiedFiles)
    }

    private Set<ModifiedMethod> getModifiedMethods(Project project, String filePath, String ancestorSHA, String targetSHA) {
        File ancestorFile = FileManager.getFileInCommit(project, filePath, ancestorSHA)
        File targetFile = FileManager.getFileInCommit(project, filePath, targetSHA)

        String[] diffJOutput = runDiffJ(ancestorFile, targetFile);
        String[] textualDiffOutput = runTextualDiff(ancestorFile, targetFile);

        Map<String, int[]> parsedDiffJResult = modifiedMethodsParser.parse(diffJOutput);
        List<ModifiedLine> parsedTextualDiffResult = textualDiffParser.parse(textualDiffOutput);

        return modifiedMethodsMatcher.matchModifiedMethodsAndLines(parsedDiffJResult, parsedTextualDiffResult);
    }

    private String[] runDiffJ(File ancestorFile, File targetFile) {
        Process diffJ = ProcessRunner.runProcess('dependencies', 'java', '-jar', 'diffj.jar', "--brief", ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(diffJ.getInputStream()))
        def output = reader.readLines()
        println "DIFFJ"
        println output
        reader.close()
        return output
    }

    private String[] runTextualDiff (File ancestorFile, File targetFile) {
        Process textDiff = ProcessRunner.runProcess(".", "diff" ,ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(textDiff.getInputStream()))
        def output = reader.readLines()
        println "TEXTUAL DIFF"
        println output
        reader.close()
        return output
    }
}