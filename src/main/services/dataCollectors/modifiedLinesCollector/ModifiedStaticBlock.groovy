package services.dataCollectors.modifiedLinesCollector

/**
 * This class represents a method's difference from a base to commit to
 * another, it uses the identifier to identify it and has a list of the modified
 * lines from one commit to another
 */
class ModifiedStaticBlock {

    private String identifier;
    private Set<ModifiedLine> modifiedLines;

    ModifiedStaticBlock(String identifier, Set<ModifiedLine> lines) {
        this.identifier = identifier;
        this.modifiedLines = lines;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public Set<ModifiedLine> getModifiedLines() {
        return this.modifiedLines;
    }

    @Override
    public boolean equals(Object modifiedStaticBlock) {
        return modifiedStaticBlock instanceof ModifiedStaticBlock && this.identifier == modifiedStaticBlock.getIdentifier()
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public String toString() {
        String result = this.identifier + "\n"
        for (def line : this.modifiedLines) {
            result += line.toString()    + "\n";
        }
        return result;
    }

}