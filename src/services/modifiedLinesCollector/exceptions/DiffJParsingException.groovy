package services.modifiedLinesCollector.exceptions

public class DiffJParsingException extends Exception {
    public DiffJParsingException(String expectedCharactere, String foundLine) {
        super("DiffJ Parsing Error: Expected a ${expectedCharactere} on line ${foundLine}");
    }
}