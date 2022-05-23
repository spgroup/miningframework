package services.dataCollectors.staticBlockCollector

import services.dataCollectors.modifiedLinesCollector.ModifiedLine

/**
 * This class represents a staticBlock's difference from a base to commit to
 * another, it uses the identifier(generated number) to identify it and has a list of the modified
 * lines from one commit to another
 */
class StaticBlock {

    private String identifier;
    private Set<ModifiedLine> modifiedLines;
    private String path;


    StaticBlock(String identifier, Set<ModifiedLine> lines) {
        this.identifier = identifier;
        this.modifiedLines = lines;
    }
    StaticBlock(String identifier, Set<ModifiedLine> lines, path) {
        this.identifier = identifier;
        this.modifiedLines = lines;
        this.path = path;
    }
    public String getIdentifier() {
        return this.identifier;
    }

    public Set<ModifiedLine> getModifiedLines() {
        return this.modifiedLines;
    }
    public String getPath(){
        return path;
    }
    @Override
    public boolean equals(Object modifiedStaticBlock) {
        return modifiedStaticBlock instanceof StaticBlock && this.identifier == modifiedStaticBlock.getIdentifier()
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