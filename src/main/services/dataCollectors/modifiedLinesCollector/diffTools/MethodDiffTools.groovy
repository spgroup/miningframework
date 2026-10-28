package services.dataCollectors.modifiedLinesCollector.diffTools

/**
 * The method diff tools by name. The one used by default is chosen with the system property
 * miningframework.diffTool (e.g. -Dminingframework.diffTool=diffj); to add a tool, add it to create.
 */
class MethodDiffTools {

    static final String PROPERTY = 'miningframework.diffTool'
    static final String DEFAULT = 'diffj'

    static MethodDiffTool create(String dependenciesPath = 'dependencies', String name = System.getProperty(PROPERTY, DEFAULT)) {
        switch (name) {
            case 'diffj':
                return new DiffJDiffTool('diffj.jar', dependenciesPath)
            default:
                throw new IllegalArgumentException("Unknown method diff tool: ${name} (${PROPERTY} can be: diffj)")
        }
    }
}
