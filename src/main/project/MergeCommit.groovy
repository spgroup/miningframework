package main.project

class MergeCommit {

    private String sha
    private String[] parentsSHA
    private String ancestorSHA

    public MergeCommit(sha, parentsSHA, ancestorSHA) {
        this.sha = sha
        this.parentsSHA = parentsSHA
        this.ancestorSHA = ancestorSHA
    }

    public boolean isOctopus() {
        return parentsSHA.length > 2
    }

    public String getSHA() {
        return sha
    }

    public void setSHA(String sha) {
        this.sha = sha
    }

    public String getLeftSHA() {
        return parentsSHA[0]
    }

    public void setLeftSHA(String sha) {
        this.parentsSHA[0] = sha
    }

    public String getRightSHA() {
        return parentsSHA[1]
    }

    public void setRightSHA(String sha) {
        this.parentsSHA[1] = sha
    }

    public String[] getParentsSHA() {
        return parentsSHA
    }

    public void setParentsSHA(String[] parentsSHA) {
        this.parentsSHA = parentsSHA
    }

    public String getAncestorSHA() {
        return ancestorSHA
    }

    public void setAncestorSHA(String ancestorSHA) {
        this.ancestorSHA = ancestorSHA
    }

}