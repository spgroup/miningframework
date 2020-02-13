package main.util

import main.util.ProcessRunner

public final class FileTransformations {

    public static void executeCodeTransformations(String fileName) throws IOException {
        Process codeTransformations = ProcessRunner.runProcess('dependencies', 'java', '-jar', 'code-transformations.jar', fileName)
    }

}