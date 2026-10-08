package services.outputProcessors.soot

import soot.G
import soot.Modifier
import soot.Scene
import soot.SootClass
import soot.SootMethod
import soot.options.Options
import util.JavaApiClassPath

import java.util.zip.ZipFile

/**
 * Soot signatures, the ones conflict-static-analysis takes as entrypoints, of the methods of a modified lines JSON,
 * named by diffj: e.g. "process(Node, List<T>)" of "a.C" -> "<a.C: void process(a.Node,java.util.List)>". The method of
 * the class, or of a class nested in it, with the same name and parameter types (see sameType; diffj omits the "..."
 * of varargs) or, failing that, the only one with as many parameters. Abstract methods and bridge methods, which javac
 * adds with the same parameters, are ignored; constructors of inner classes have the outer instance as a first
 * parameter. Methods not found (e.g. whose signature changed from base, by which diffj names them, or with the same
 * name and parameters in more than one nested class) and fields are left out.
 */
class SootSignatures {

    private static final int VARARGS = 0x0080 // ACC_VARARGS of the class file, kept by Soot in the method modifiers
    private static final Set<String> PRIMITIVES = ['boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double'] as Set

    static Set<String> of(String jar, List<Map> methods) {
        G.reset()
        Options.v().set_allow_phantom_refs(true)
        Options.v().set_soot_classpath([jar, JavaApiClassPath.get()].join(File.pathSeparator))
        List<String> jarClasses = new ZipFile(jar).withCloseable { ZipFile zip ->
            zip.entries().toList()*.name.findAll { it.endsWith('.class') }.collect { it.replace('/', '.') - '.class' }
        }

        Set<String> signatures = new TreeSet<>()
        for (Map method : methods) {
            def matcher = method.method =~ /^([\w$]+)\((.*)\)$/
            if (!matcher.matches()) {
                continue
            }

            List<String> parameters = erase(matcher.group(2)).split(',')*.trim().findAll().collect { simpleName(it) }
            List<SootMethod> sameNameMethods = jarClasses.findAll { it == method.className || it.startsWith("${method.className}\$") }
                    .collectMany { Scene.v().loadClass(it, SootClass.SIGNATURES).methods }
                    .findAll { it.concrete && !Modifier.isSynthetic(it.modifiers) && (it.constructor ? simpleName(it.declaringClass.name) : it.name) == matcher.group(1) }

            Closure<List<String>> sootParameters = { SootMethod sootMethod ->
                List<String> types = sootMethod.parameterTypes.collect { simpleName(it.toString()) }
                boolean innerConstructor = sootMethod.constructor && sootMethod.declaringClass.name.contains('$')
                innerConstructor && types.size() == parameters.size() + 1 ? types.drop(1) : types
            }
            Closure<Boolean> sameParameters = { SootMethod sootMethod ->
                List<String> types = sootParameters(sootMethod)
                boolean varargs = (sootMethod.modifiers & VARARGS) != 0
                types.size() == parameters.size() && types.indices.every { int i ->
                    sameType(parameters[i], types[i]) || (varargs && i == types.size() - 1 && sameType(parameters[i] + '[]', types[i]))
                }
            }

            List<SootMethod> matchingMethods = sameNameMethods.findAll(sameParameters) ?:
                    sameNameMethods.findAll { sootParameters(it).size() == parameters.size() }
            if (matchingMethods.size() == 1) {
                signatures << matchingMethods[0].signature
            } else {
                println "${method.className}: ${method.method} not found in ${jar}"
            }
        }
        return signatures
    }

    // simple names of a diffj and a Soot type are the same, or the diffj one is a type variable (e.g. T, T[]), erased
    // in bytecode to its bound: any reference type with as many dimensions
    private static boolean sameType(String diffjType, String sootType) {
        def variable = diffjType =~ /^[A-Z]\d?((?:\[\])*)$/
        String element = variable.matches() ? sootType - variable.group(1) : null
        return diffjType == sootType || (element && sootType.endsWith(variable.group(1)) && !element.contains('[') && !PRIMITIVES.contains(element))
    }

    private static String erase(String type) {
        String erased = type
        while (erased =~ /<[^<>]*>/) {
            erased = erased.replaceAll(/<[^<>]*>/, '')
        }
        return erased
    }

    // e.g. "Map.Entry", "java.util.Map$Entry" and "Entry" -> "Entry"; "String..." -> "String[]"
    private static String simpleName(String type) {
        return type.replace('...', '[]').split(/[.$]/)[-1]
    }
}
