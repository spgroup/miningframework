package main.exception

public class NoAccessKeyException extends Exception {
    public NoAccessKeyException() {
        super("To use forks you need to specify a Github access key")
    }
}