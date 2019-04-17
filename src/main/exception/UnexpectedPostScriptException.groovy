package main.exception

public class UnexpectedPostScriptException extends Exception {
    public UnexpectedPostScriptException(String message) {
        super("There was an error running your post script: \"${message}\"")
    }
}