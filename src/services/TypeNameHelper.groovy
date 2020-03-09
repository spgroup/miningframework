package services

import java.util.regex.Matcher
import java.util.regex.Pattern

import main.project.Project

import main.util.ProcessRunner

class TypeNameHelper {

    static public String getFullyQualifiedName(Project project, String filePath, String SHA) {
        String name = getName(filePath)
        String packageName = getPackage(project, SHA, filePath)

        return (packageName == "" ? "" : packageName + '.') + name
    }

    static private String getPackage(Project project, String SHA, String filePath) {
        Process gitCatFile = ProcessRunner.runProcess(project.getPath(), 'git', 'cat-file', '-p', "${SHA}:${filePath}")
        
        def fileLines = gitCatFile.getInputStream().readLines()

        for (String fileLine : fileLines) {
            String lineNoWhitespace = fileLine.trim()
            if(lineNoWhitespace.take(7) == 'package') {
                String packageName;
                if (lineNoWhitespace.endsWith(";")) {
                    packageName = lineNoWhitespace.substring(7, lineNoWhitespace.indexOf(';'))
                } else {
                    packageName = lineNoWhitespace.substring(7, lineNoWhitespace.length() - 1)
                }
                return packageName;
            }
        }
        
        return "";
    }

    static private String getName(String filePath) {
        // this uses two built in identifiers used in java to represent the Patterns that recognize 
        // java valid indetifiers
        Pattern pattern = Pattern.compile("(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)\\.java") 
        Matcher matcher = pattern.matcher(filePath)
        if(matcher.find())
            return matcher.group(1)
    }
}