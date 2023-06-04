package services.dataCollectors.staticBlockCollector

/**
 * This class represents a staticBlock's , it uses the identifier(generated number) to identify a initialization block
 *  which file does this initialization block belong to
 */
class StaticBlock {

    private String identifier;
    private String path;

    StaticBlock(String identifier, path) {
        this.identifier = identifier;
        this.path = path;
    }
    public String getIdentifier() {
        return this.identifier;
    }

    public String getPath(){
        return path;
    }
    public void setPath(String pPath){
        this.path = pPath;
    }
    @Override
    public boolean equals(Object modifiedStaticBlock) {
        return modifiedStaticBlock instanceof StaticBlock && this.identifier == modifiedStaticBlock.getIdentifier()
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }
}