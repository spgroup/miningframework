package services.modifiedLinesCollector

class ModifiedMethod {

    private String signature;
    private Set<ModifiedLine> lines;

    ModifiedMethod(String signature, Set<ModifiedLine> lines) {
        this.signature = signature;
        this.lines = lines;
    }

    public String getSignature() {
        return this.signature;
    }

    public Set<ModifiedLine> getLines() {
        return this.lines;
    }

    @Override
    public boolean equals(Object modifiedMethod) {
        return this.signature.equals(modifiedMethod.getSignature())
    }

    @Override
    public int hashCode() {
        return this.signature.hashCode();
    }

    @Override
    public String toString() {
        String result = this.signature + "\n"
        for (def line : this.lines) {
            result += line.toString()    + "\n";
        }
        return result;
    }

}