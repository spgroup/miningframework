class ModifiedMethod {
    private String signature
    private Set<Integer> modifiedLines

    public ModifiedMethod(String signature, Set<Integer> modifiedLines) {
        this.signature = signature
        this.modifiedLines = modifiedLines
    }

    public String getSignature() {
        return signature
    }

    public void setSignature(String signature) {
        this.signature = signature
    }

    public Set<Integer> getModifiedLines() {
        return modifiedLines
    }

    public void setModifiedLines(Set<Integer> modifiedLines) {
        this.modifiedLines = modifiedLines
    }

    public String toString() {
        return signature + ' ' + modifiedLines
    }
}