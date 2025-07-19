package arguments

import injectors.StaticAnalysisConflictsDetectionModule
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator

class Arguments {

    private String inputPath
    private String outputPath
    private String sinceDate
    private String untilDate
    private Class injector
    private boolean isHelp
    private String resultsRemoteRepositoryURL
    private String accessKey
    private int numOfThreads
    private boolean keepProjects
    private String syntacticSeparators
    private String fileExtension
    private Level logLevel

    Arguments() { // set the default values for all parameters
        isHelp = false
        sinceDate = ''
        untilDate = ''
        outputPath = 'output'
        injector = StaticAnalysisConflictsDetectionModule
        resultsRemoteRepositoryURL = ''
        accessKey = ''
        numOfThreads = 1
        keepProjects = false
        syntacticSeparators = '{ } ( ) ; ,'
        fileExtension = '.java'
        logLevel = Level.INFO
    }

    void setNumOfThreads(int numOfThreads) {
        this.numOfThreads = numOfThreads
    }

    void setInputPath(String inputPath) {
        this.inputPath = inputPath
    }

    void setOutputPath(String outputPath) {
        this.outputPath = outputPath
    }

    void setSinceDate(String sinceDate) {
        this.sinceDate = sinceDate
    }

    void setUntilDate(String untilDate) {
        this.untilDate = untilDate
    }

    Class setInjector(Class injector) {
        this.injector = injector
    }

    void setResultsRemoteRepositoryURL(String resultsRemoteRepositoryURL) {
        this.resultsRemoteRepositoryURL = resultsRemoteRepositoryURL
    }

    boolean setHelp() {
        this.isHelp = true
    }

    void setAccessKey(String accessKey) {
        this.accessKey = accessKey
    }

    void setKeepProjects() {
        this.keepProjects = true;
    }

    void setFileExtension(String fileExt) {
        this.fileExtension = fileExt
    }

    void setLanguageSyntacticSeparators(String separators) {
        this.syntacticSeparators = separators
    }

    int getNumOfThreads() {
        return this.numOfThreads
    }

    String getInputPath() {
        return inputPath
    }

    String getOutputPath() {
        return outputPath
    }

    String getSinceDate() {
        return sinceDate
    }

    String getUntilDate() {
        return untilDate
    }

    Class getInjector() {
        return injector
    }

    boolean isHelp() {
        return isHelp
    }

    boolean getKeepProjects() {
        return keepProjects
    }

    String getResultsRemoteRepositoryURL() {
        return resultsRemoteRepositoryURL
    }

    String getAccessKey() {
        return accessKey
    }

    String getFileExtension() {
        return fileExtension
    }

    String getLanguageSyntacticSeparators() {
        return syntacticSeparators
    }

    boolean providedAccessKey() {
        return accessKey.length() > 0
    }

    boolean isPushCommandActive() {
        return !resultsRemoteRepositoryURL.equals('')
    }

    Level getLogLevel() {
        return logLevel
    }

    void setLogLevel(Level logLevel) {
        this.logLevel = logLevel
        Configurator.setRootLevel(logLevel)
    }
}