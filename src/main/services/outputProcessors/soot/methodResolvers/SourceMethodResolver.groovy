package services.outputProcessors.soot.methodResolvers

import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.*
import soot.Scene
import util.JavaApiClassPath

/**
 * Finds the methods of the modified lines in the source code of the merge, with the Eclipse JDT parser.
 * Unlike BytecodeMethodResolver, a line without bytecode (e.g. "if (true)") still has its method.
 *
 * How it works:
 *   1. JDT parses the Java file and resolves its types, using the build and the Java API as class path
 *      (to know that "Node" is "b.Node"). No line is looked up in the bytecode.
 *   2. Each line goes to the smallest declaration that contains it: a method, constructor, initializer block,
 *      field or enum constant.
 *   3. The declaration becomes the Soot signatures of the methods that run its code, as javac compiles it:
 *      - method or constructor: itself, e.g. "<a.C: void m(b.Node)>";
 *      - static field or static block: "<a.C: void <clinit>()>";
 *      - instance field or block: the constructors that do not call this(...);
 *      - constant (static final with a constant value): none, javac inlines it.
 *
 * A method with a type JDT cannot resolve gives no signature, as does the constructor of an anonymous or local class.
 */
class SourceMethodResolver implements MethodResolver {

    Set<String> resolve(String buildJarPath, String sourceDirectory, Map<String, List<Integer>> definitions) {
        Set<String> signatures = new TreeSet<>()
        definitions.each { String className, List<Integer> lines ->
            File javaFile = new File(sourceDirectory, simpleNameOfTopLevelClass(className) + '.java')
            if (lines.isEmpty() || !javaFile.exists()) {
                return
            }
            CompilationUnit code = parseWithTypes(javaFile, className, buildJarPath)
            for (int line : lines) {
                BodyDeclaration declaration = findDeclarationContaining(code, line)
                if (declaration != null) {
                    signatures.addAll(methodsThatRun(declaration, code, line))
                }
            }
        }
        return signatures
    }

    // "a.C$Inner" -> "C"
    private static String simpleNameOfTopLevelClass(String className) {
        return className.tokenize('.').last().tokenize('$').first()
    }

    private static CompilationUnit parseWithTypes(File javaFile, String className, String buildJarPath) {
        ASTParser parser = ASTParser.newParser(AST.JLS8)
        Map<String, String> options = JavaCore.getOptions()
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options)
        parser.setCompilerOptions(options)
        parser.setKind(ASTParser.K_COMPILATION_UNIT)
        parser.setResolveBindings(true)

        List<String> classPath = [JavaApiClassPath.get()]
        if (new File(buildJarPath).exists()) {
            classPath.add(0, buildJarPath)
        }
        parser.setEnvironment(classPath as String[], null, null, false)
        parser.setUnitName(className.tokenize('$').first().replace('.', '/') + '.java')
        parser.setSource(javaFile.getText('UTF-8').toCharArray())
        return (CompilationUnit) parser.createAST(null)
    }

    // The smallest method, constructor, initializer block, field or enum constant with the line
    private static BodyDeclaration findDeclarationContaining(CompilationUnit code, int line) {
        BodyDeclaration smallest = null
        code.accept(new ASTVisitor() {
            @Override
            void preVisit(ASTNode node) {
                boolean isDeclaration = node instanceof BodyDeclaration && !(node instanceof AbstractTypeDeclaration)
                if (isDeclaration && containsLine(code, (BodyDeclaration) node, line)) {
                    if (smallest == null || node.getLength() < smallest.getLength()) {
                        smallest = (BodyDeclaration) node
                    }
                }
            }
        })
        return smallest
    }

    // Javadoc is not part of the declaration
    private static boolean containsLine(CompilationUnit code, BodyDeclaration declaration, int line) {
        Javadoc javadoc = declaration.getJavadoc()
        int start = javadoc != null ? javadoc.getStartPosition() + javadoc.getLength() : declaration.getStartPosition()
        int end = declaration.getStartPosition() + declaration.getLength() - 1
        return code.getLineNumber(start) <= line && line <= code.getLineNumber(end)
    }

    // The methods of the bytecode that run the code of the declaration, as javac compiles it
    private static List<String> methodsThatRun(BodyDeclaration declaration, CompilationUnit code, int line) {
        ASTNode classNode = declaration.getParent()

        if (declaration instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration) declaration
            if (method.getBody() == null) {
                return [] // abstract or interface method: no code
            }
            String signature = sootSignature(method)
            return signature == null ? [] : [signature]
        }

        if (declaration instanceof Initializer) {
            boolean isStatic = Modifier.isStatic(declaration.getModifiers())
            return isStatic ? staticInitializerOf(classNode) : constructorsThatInitializeFields(classNode)
        }

        if (declaration instanceof EnumConstantDeclaration) {
            return staticInitializerOf(classNode)
        }

        if (declaration instanceof FieldDeclaration) {
            return methodsThatInitializeField((FieldDeclaration) declaration, code, line)
        }
        return []
    }

    /*
     * The methods that run the values of the fields of the line: in "int a, b = compute();" both fields are on the
     * line, and b runs code. The fields of a declaration share its modifiers, so all of them run in the same methods.
     */
    private static List<String> methodsThatInitializeField(FieldDeclaration declaration, CompilationUnit code, int line) {
        List<VariableDeclarationFragment> fields = fieldsOfLine(declaration, code, line)
        List<IVariableBinding> fieldsThatRunCode = fields.findAll { it.getInitializer() != null }.collect { it.resolveBinding() }.findAll {
            it != null && !isConstant(it) // a field without value has no code, and javac inlines the value of a constant
        }
        if (fieldsThatRunCode.isEmpty()) {
            return []
        }
        return isStatic(fieldsThatRunCode.first()) ? staticInitializerOf(declaration.getParent()) : constructorsThatInitializeFields(declaration.getParent())
    }

    /*
     * The fields that are on the line ("int a = 0, b = 2;" has a and b); all of them on a line before the first one
     * (e.g. the type, in a declaration over many lines); else the first one that ends after the line (e.g. a line
     * with only a comment between two fields)
     */
    private static List<VariableDeclarationFragment> fieldsOfLine(FieldDeclaration declaration, CompilationUnit code, int line) {
        List<VariableDeclarationFragment> fields = declaration.fragments()
        if (line < code.getLineNumber(fields.first().getStartPosition())) {
            return fields
        }
        List<VariableDeclarationFragment> onTheLine = fields.findAll { code.getLineNumber(it.getStartPosition()) <= line && line <= lastLineOf(it, code) }
        if (!onTheLine.isEmpty()) {
            return onTheLine
        }
        VariableDeclarationFragment next = fields.find { lastLineOf(it, code) >= line }
        return next == null ? [] : [next]
    }

    private static int lastLineOf(ASTNode node, CompilationUnit code) {
        return code.getLineNumber(node.getStartPosition() + node.getLength() - 1)
    }

    private static boolean isStatic(IVariableBinding field) {
        return Modifier.isStatic(field.getModifiers()) || field.getDeclaringClass()?.isInterface()
    }

    private static boolean isConstant(IVariableBinding field) {
        return isStatic(field) && Modifier.isFinal(field.getModifiers()) && field.getConstantValue() != null
    }

    // e.g. "<a.C: void process(b.Node,int)>", or null when JDT cannot resolve a type
    private static String sootSignature(MethodDeclaration method) {
        IMethodBinding binding = method.resolveBinding()?.getMethodDeclaration()
        String className = binaryClassName(method.getParent())
        if (binding == null || className == null) {
            return null
        }

        List<String> parameters = binding.getParameterTypes().collect { sootTypeName(it) }
        if (binding.isConstructor()) {
            List<String> addedByJavac = constructorParametersAddedByJavac(binding.getDeclaringClass())
            if (addedByJavac == null) {
                return null
            }
            parameters = addedByJavac + parameters
        }
        String returnType = binding.isConstructor() ? 'void' : sootTypeName(binding.getReturnType())
        if (parameters.contains(null) || returnType == null) {
            return null
        }

        String name = binding.isConstructor() ? '<init>' : Scene.v().quotedNameOf(binding.getName())
        return "<${className}: ${returnType} ${name}(${parameters.join(',')})>"
    }

    private static List<String> staticInitializerOf(ASTNode classNode) {
        String className = binaryClassName(classNode)
        return className == null ? [] : ["<${className}: void <clinit>()>".toString()]
    }

    // The constructors that run the field values and initializer blocks: the ones that do not call this(...)
    private static List<String> constructorsThatInitializeFields(ASTNode classNode) {
        if (!(classNode instanceof AbstractTypeDeclaration)) {
            return [] // anonymous class: javac makes up its constructor
        }
        List<MethodDeclaration> constructors = ((AbstractTypeDeclaration) classNode).bodyDeclarations().findAll {
            it instanceof MethodDeclaration && it.isConstructor()
        }

        if (constructors.isEmpty()) {
            // the default constructor, which javac adds
            String className = binaryClassName(classNode)
            ITypeBinding type = ((AbstractTypeDeclaration) classNode).resolveBinding()
            List<String> parameters = type == null ? null : constructorParametersAddedByJavac(type)
            if (className == null || parameters == null) {
                return []
            }
            return ["<${className}: void <init>(${parameters.join(',')})>".toString()]
        }

        List<String> signatures = []
        for (MethodDeclaration constructor : constructors) {
            List statements = constructor.getBody()?.statements() ?: []
            boolean callsThis = statements && statements.first() instanceof ConstructorInvocation
            String signature = callsThis ? null : sootSignature(constructor)
            if (signature != null) {
                signatures.add(signature)
            }
        }
        return signatures
    }

    /*
     * The parameters javac adds to a constructor: the name and position of an enum constant, or the outer object of
     * an inner class. Null for anonymous and local classes, whose added parameters we do not know.
     */
    private static List<String> constructorParametersAddedByJavac(ITypeBinding type) {
        if (type.isAnonymous() || type.isLocal()) {
            return null
        }
        if (type.isEnum()) {
            return ['java.lang.String', 'int']
        }
        boolean isInnerClass = type.isMember() && !Modifier.isStatic(type.getModifiers()) &&
                !type.isInterface() && !type.getDeclaringClass().isInterface()
        if (isInnerClass) {
            return [sootTypeName(type.getDeclaringClass())]
        }
        return []
    }

    /*
     * The class name as javac gives it: "a.C", "a.C$Inner", and "a.C$2" for the second anonymous class of a.C.
     * We count the anonymous classes ourselves, in the order of the source, because JDT sometimes numbers them
     * otherwise (in Gson ConstructorConstructor it gives $8 to javac's $9).
     */
    private static String binaryClassName(ASTNode classNode) {
        if (classNode instanceof AnonymousClassDeclaration) {
            ASTNode outerClass = enclosingClassOf(classNode.getParent())
            String outerName = outerClass == null ? null : binaryClassName(outerClass)
            if (outerName == null) {
                return null
            }
            int number = anonymousClassesOf(outerClass).findIndexOf { it.is(classNode) } + 1
            return outerName + '$' + number
        }

        if (classNode instanceof AbstractTypeDeclaration) {
            AbstractTypeDeclaration namedClass = (AbstractTypeDeclaration) classNode
            ASTNode parent = namedClass.getParent()
            boolean isNestedClass = parent instanceof AbstractTypeDeclaration || parent instanceof AnonymousClassDeclaration
            if (isNestedClass) {
                String outerName = binaryClassName(parent)
                return outerName == null ? null : outerName + '$' + namedClass.getName().getIdentifier()
            }
            return namedClass.resolveBinding()?.getBinaryName() // top-level or local class
        }
        return null
    }

    // The anonymous classes directly inside the class, in the order of the source
    private static List<ASTNode> anonymousClassesOf(ASTNode classNode) {
        List<ASTNode> anonymousClasses = []
        classNode.accept(new ASTVisitor() {
            @Override
            boolean visit(AnonymousClassDeclaration node) {
                if (enclosingClassOf(node.getParent()).is(classNode)) {
                    anonymousClasses.add(node)
                }
                return true
            }
        })
        return anonymousClasses
    }

    // The closest named or anonymous class around the node
    private static ASTNode enclosingClassOf(ASTNode node) {
        while (node != null && !(node instanceof AbstractTypeDeclaration) && !(node instanceof AnonymousClassDeclaration)) {
            node = node.getParent()
        }
        return node
    }

    /*
     * The type as Soot writes it: without generics, with $ for nested classes and quotes in Jimple keywords,
     * e.g. "java.util.List", "java.util.Map$Entry[]", "java.lang.'annotation'.Annotation".
     * Null when JDT could not resolve it.
     */
    private static String sootTypeName(ITypeBinding type) {
        if (type == null || type.isRecovered()) {
            return null
        }
        if (type.isArray()) {
            String elementType = sootTypeName(type.getElementType())
            return elementType == null ? null : elementType + '[]' * type.getDimensions()
        }
        ITypeBinding withoutGenerics = type.getErasure()
        if (withoutGenerics.isPrimitive()) {
            return withoutGenerics.getName()
        }
        return Scene.v().quotedNameOf(withoutGenerics.getBinaryName())
    }
}
