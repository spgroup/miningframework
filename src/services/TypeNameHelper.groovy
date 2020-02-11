package services

import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

import main.project.Project

import main.util.ProcessRunner

class TypeNameHelper {

    static public String getFullyQualifiedName(Project project, String filePath, String SHA) {
        String name = getName(filePath)
        String package = getPackage(project, SHA, filePath)

        return (package == "" ? "" : package + '.') + name
    }

    static private String getPackage(Project project, String SHA, String filePath) {
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

    static private String getName(String filePath) {
        // this uses two built in identifiers used in java to represent the Patterns that recognize 
        // java valid indetifiers
        Pattern pattern = Pattern.compile("(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)\\.java") 
        Matcher matcher = pattern.matcher(filePath)
        if(matcher.find())
            return matcher.group(1)
    }
}