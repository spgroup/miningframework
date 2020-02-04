package services

import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

import main.project.Project

import main.util.ProcessRunner

class ClassNameHelper {

    static public String getClassFullyQualifiedName(Project project, String filePath, String SHA) {
        String className = getClassName(filePath)
        String classPackage = getClassPackage(project, SHA, filePath)

        return (classPackage == "" ? "" : classPackage + '.') + className
    }

    static private String getClassPackage(Project project, String SHA, String filePath) {
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

    static private String getClassName(String filePath) {
        Pattern pattern = Pattern.compile("/?([A-Z][A-Za-z0-9]*?)\\.java") // find the name of the class by the name of the file
        Matcher matcher = pattern.matcher(filePath)
        if(matcher.find())
            return matcher.group(1)
    }
}