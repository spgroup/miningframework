package services.modifiedLinesCollector.exceptions

class TextualDiffParsingException extends Exception {
    TextualDiffParsingException(String expectedCharacter, String foundLine) {
        super("Textual Diff Parsing Error: Expected a ${expectedCharacter} on line ${foundLine}");
    }
}

