package util

public class FileTransformations {

    static public Process executeCodeTransformations(String fileName) throws IOException {
        return ProcessRunner.runProcess('dependencies', 'java', '-jar', 'code-transformations.jar', fileName)
    }

}