package services


public class ModifiedLine {

    private String content;
    private int number;
    private String method;
    private Tuple2 deletedNumbers;
    private ExperimentalDataCollectorImpl$Modification type;

    public ModifiedLine(String content, int number, ExperimentalDataCollectorImpl$Modification type) {
        this.content = content;
        this.number = number;
        this.type = type;
    }

    public ModifiedLine(String content, Tuple2 numbers, ExperimentalDataCollectorImpl$Modification type) {
        this.content = content;
        this.deletedNumbers = numbers;
        this.type = type;
    }

    public getContent() {
        return content;
    }

    public getNumber() {
        return number;
    }

    public getDeletedLineNumbersTuple() {
        return deletedNumbers;
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