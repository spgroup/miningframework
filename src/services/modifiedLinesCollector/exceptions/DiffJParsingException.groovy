package services.modifiedLinesCollector.exceptions

public class DiffJParsingException extends Exception {
    public DiffJParsingException(String expectedCharacter, String foundLine) {
        super("DiffJ Parsing Error: Expected a ${expectedCharacter} on line ${foundLine}");
    }
}