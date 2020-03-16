package main.exception

/*
* This exception is meant to be called everytime the execution depends on the script returning success
*/
public class ExternalScriptException extends Exception {
    public ExternalScriptException(String scriptName, int statusCode) {
        super(scriptName + " ended with a non-zero exit code: " + statusCode)
    }
}