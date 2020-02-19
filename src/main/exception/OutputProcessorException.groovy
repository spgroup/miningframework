package main.exception

public class OutputProcessorException extends Exception {
    public OutputProcessorException(String message) {
        super("There was an error on the OutputProcessor implementation: " + message);
    }
}