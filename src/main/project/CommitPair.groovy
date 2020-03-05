package main.project

class CommitPair {

    private String shaCommitOne
    private String shaCommitTwo
    private String localPathCommitOne = ""
    private String localPathCommitTwo = ""

    public CommitPair(shaCommitOne, shaCommitTwo) {
        this.shaCommitOne = shaCommitOne
        this.shaCommitTwo = shaCommitTwo
    }

    public CommitPair(shaCommitOne, shaCommitTwo, localPathCommitOne, localPathCommitTwo) {
        this.shaCommitOne = shaCommitOne
        this.shaCommitTwo = shaCommitTwo
        this.localPathCommitOne = localPathCommitOne
        this.localPathCommitTwo = localPathCommitTwo
    }

    public String getSHACommitOne() {
        return shaCommitOne
    }

    public String getSHACommitTwo() {
        return shaCommitTwo
    }

    public void setSHACommitOne(String sha) {
        this.shaCommitOne = sha
    }

    public void setSHACommitTwo(String sha) {
        this.shaCommitTwo = sha
    }

    public String getLocalPathCommitOne() {
        return localPathCommitOne
    }

    public String getLocalPathCommitTwo() {
        return localPathCommitTwo
    }

    public void setLocalPathCommitOne(String path) {
        this.localPathCommitOne = path
    }

    public void setLocalPathCommitTwp(String path) {
        this.localPathCommitTwo = path
    }

}