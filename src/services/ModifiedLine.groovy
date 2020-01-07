package services

import static services.ExperimentalDataCollectorImpl.ModificationType;
public class ModifiedLine {

    private String content;
    private int number;
    private String method;
    private Tuple2 deletedNumbers;
    private ModificationType type;

    public ModifiedLine(String content, int number, ModificationType type) {
        this.content = content;
        this.number = number;
        this.type = type;
    }

    public ModifiedLine(String content, Tuple2 numbers, ModificationType type) {
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

    @Override  
    public int hashCode() {
        return (content + type).hashCode()
    }
}