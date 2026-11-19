package services.dataCollectors.modifiedLinesCollector.diffTools

import static app.MiningFramework.arguments

/**
 * The method diff tools by name. The one used by default is the --diff-tool option of the miningframework
 * (arguments.ArgsParser); to add a tool, add it to NAMES and create.
 */
class MethodDiffTools {

    static final List<String> NAMES = ['gumtree', 'diffj']
    static final String DEFAULT = 'diffj'

    static MethodDiffTool create(String dependenciesPath = 'dependencies', String name = arguments?.getDiffTool() ?: DEFAULT) {
        switch (name) {
            case 'gumtree':
                return new GumTreeDiffTool()
            case 'diffj':
                return new DiffJDiffTool('diffj.jar', dependenciesPath)
            default:
                throw new IllegalArgumentException("Unknown method diff tool: ${name} (can be: ${NAMES.join(', ')})")
        }
    }
}
