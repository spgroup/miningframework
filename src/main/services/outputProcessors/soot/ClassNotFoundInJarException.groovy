package services.outputProcessors.soot

class ClassNotFoundInJarException extends Exception {
    ClassNotFoundInJarException(String className) {
        super(className + " class not found in any of the downloaded jars")
    }

}
