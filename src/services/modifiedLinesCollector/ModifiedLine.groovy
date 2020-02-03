package services.modifiedLinesCollector

class ModifiedLine {

    public enum ModificationType {
        Added,
        Changed,
        Removed
    }

    private int number;
    private String content;
    private ModificationType type;

    ModifiedLine(int number, String content, ModificationType type) {
        this.number = number;
        this.content = content;
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public String getContent() {
        return content;
    }

    public ModificationType getType() {
        return type;
    }

    public String toString() {
        return number + "-" + content + "-" + type
    }

    @Override
    public boolean equals(Object modifiedLine) {
        return modifiedLine.getContent().equals(this.content);
    }

    @Override
    public int hashCode() {
        return (this.content).hashCode();
    }
}