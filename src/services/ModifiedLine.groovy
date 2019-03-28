package services

public class ModifiedLine {

    public static enum CONTRIBUTION {Left, Right}

    private String content;
    private int number;
    private String method;
    private DataCollectorImpl$Modification type;
    private CONTRIBUTION contribution;

    public ModifiedLine(String content, int number, DataCollectorImpl$Modification type) {
        this.content = content;
        this.number = number;
        this.type = type;
    }

    public getContent() {
        return content;
    }

    public getNumber() {
        return number;
    }

    public getType() {
        return type;
    }

    public String toString() {
        return Integer.toString(number) + " " + content + " " + type
    }

    public boolean equals(Object o) {
        return content.equals(o.content) && type == o.type
    }

}