package services

class ModifiedDeclaration {
    private String signature
    private Set<ModifiedLine> modifiedLines

    public ModifiedDeclaration(String signature, Set<ModifiedLine> modifiedLines) {
        this.signature = signature
        this.modifiedLines = modifiedLines
    }

    public String getSignature() {
        return signature
    }

    public void setSignature(String signature) {
        this.signature = signature
    }

    public addAllLines(Set<ModifiedLine> lines) {
        modifiedLines.addAll(lines)
    } 

    public Set<ModifiedLine> getModifiedLines() {
        return modifiedLines
    }

    public void setModifiedLines(Set<ModifiedLine> modifiedLines) {
        this.modifiedLines = modifiedLines
    }

    public String toString() {
        return signature + ' ' + modifiedLines
    }

    public boolean equals(Object o) {
        return signature.equals(o.signature)
    }
}