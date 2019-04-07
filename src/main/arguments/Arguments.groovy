package main.arguments

import services.MiningModule

class Arguments {
    
    private String inputPath
    private String outputPath
    private String sinceDate
    private String untilDate
    private Class injector
    private boolean isHelp
    private String resultsRemoteRepositoryURL

    Arguments() {
        isHelp = false
        sinceDate = ''
        untilDate = ''
        outputPath = 'output'
        injector = MiningModule
        resultsRemoteRepositoryURL = ''
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
        isHelp = true
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

    String getResultsRemoteRepositoryURL() {
        return resultsRemoteRepositoryURL
    }

    boolean isPushCommandActive() {
        return !resultsRemoteRepositoryURL.equals('')
    }
}