package main.exception

public class ProjectProcessorException extends Exception {
    public ProjectProcessorException(String message) {
        super("There was an error on the ProjectProcessor implementation: " + message);
    }
}