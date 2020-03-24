package exception

public class UnexpectedOutputException extends Exception {
    public UnexpectedOutputException(String message, String expectedFormat, String retrievedLine) {
        super("${message}\nExpected: ${expectedFormat}\nGot: ${retrievedLine}")
    }
}