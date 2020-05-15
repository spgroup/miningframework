package exception

public class UnstagedChangesException extends Exception {
    public UnstagedChangesException(String projectName) {
        super("Local project: '${projectName}' has unstaged changes, please commit or stash them before running the framework")
    }
}