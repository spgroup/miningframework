package interfaces

/**
 * Chooses the entrypoints of the static analyses of a merge scenario.
 * Methods are identified by their Soot signature, e.g. <org.example.Main: void main(java.lang.String[])>
 */
public interface EntrypointStrategy {

    public List<String> getEntrypoints(String buildJarPath, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods)

}
