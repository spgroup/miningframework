package services.dataCollectors.modifiedLinesCollector

/**
 * This class represents a method's difference from a base to commit to
 * another, it uses the signature to identify it and has a list of the modified
 * lines from one commit to another
 */
class ModifiedMethod {

    private String signature;
    private Set<ModifiedLine> modifiedLines;

    ModifiedMethod(String signature, Set<ModifiedLine> lines) {
        this.signature = signature;
        this.modifiedLines = lines;
    }

    public String getSignature() {
        return this.signature;
    }

    public Set<ModifiedLine> getModifiedLines() {
        return this.modifiedLines;
    }

    @Override
    public boolean equals(Object modifiedMethod) {
        return modifiedMethod instanceof ModifiedMethod && this.signature == modifiedMethod.getSignature()
    }

    @Override
    public int hashCode() {
        return this.signature.hashCode();
    }

    @Override
    public String toString() {
        String result = this.signature + "\n"
        for (def line : this.modifiedLines) {
            result += line.toString()    + "\n";
        }
        return result;
    }

}