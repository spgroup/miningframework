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
    private String postScript
    private String buildToken

    Arguments() {
        isHelp = false
        sinceDate = ''
        untilDate = ''
        outputPath = 'output'
        injector = MiningModule
        resultsRemoteRepositoryURL = ''
        postScript = ''
        buildToken = ''
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

    void setPostScript(String script) {
        this.postScript = script
    }

    void setBuildToken(String buildToken) {
        this.buildToken = buildToken
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

    String getPostScript () {
        return postScript
    }

    String getBuildToken() {
        return buildToken
    }
    
    boolean isPushCommandActive() {
        return !resultsRemoteRepositoryURL.equals('')
    }

}