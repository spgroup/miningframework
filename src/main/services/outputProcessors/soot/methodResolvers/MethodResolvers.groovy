package services.outputProcessors.soot.methodResolvers

/**
 * The method resolvers by name. The one used is the --method-resolver option of services.outputProcessors.soot.Main;
 * to add a resolver, add it to NAMES and create.
 */
class MethodResolvers {

    static final List<String> NAMES = ['bytecode', 'source']
    static final String DEFAULT = 'bytecode'

    static MethodResolver create(String name = DEFAULT) {
        switch (name) {
            case 'bytecode':
                return new BytecodeMethodResolver()
            case 'source':
                return new SourceMethodResolver()
            default:
                throw new IllegalArgumentException("Unknown method resolver: ${name} (can be: ${NAMES.join(', ')})")
        }
    }
}
