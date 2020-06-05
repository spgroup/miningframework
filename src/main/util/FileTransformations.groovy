package main.util

import util.ProcessRunner

public class FileTransformations {

    static public Process executeCodeTransformations(String fileName) throws IOException {
        return ProcessRunner.runProcess('dependencies', 'java', '-jar', 'code-transformations.jar', fileName)
    }

}