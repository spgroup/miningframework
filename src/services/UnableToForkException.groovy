package services

public class UnableToForkException extends Exception {
    public UnableToForkException(String message) {
        super(message)
    }
}