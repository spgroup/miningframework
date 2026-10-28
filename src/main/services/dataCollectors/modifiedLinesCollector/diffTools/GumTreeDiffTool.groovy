package services.dataCollectors.modifiedLinesCollector.diffTools

import com.github.gumtreediff.actions.EditScript
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator
import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.actions.model.Insert
import com.github.gumtreediff.actions.model.Move
import com.github.gumtreediff.actions.model.TreeInsert
import com.github.gumtreediff.actions.model.Update
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator
import com.github.gumtreediff.matchers.MappingStore
import com.github.gumtreediff.matchers.Matchers
import com.github.gumtreediff.tree.Tree
import services.dataCollectors.modifiedLinesCollector.ModifiedMethod
import util.ProcessRunner

/**
 * GumTree (AST diff of Java with the Eclipse JDT parser) as the method diff tool.
 *
 * The textual diff (diff) gives the modified lines. A hunk of the textual diff counts when GumTree changed code in one
 * of its lines (so changes only in comments or in formatting do not count, as in diffj), and each of its lines goes to
 * the method, field or initializer block that contains it: in the target, or in the ancestor for removed lines. The
 * names are the ones diffj gives, e.g. "m(String, int)", "TABLE", "static block"; a change in an anonymous class goes
 * to the method that creates it.
 */
class GumTreeDiffTool implements MethodDiffTool {

    private static final List<String> DECLARATIONS = ['MethodDeclaration', 'FieldDeclaration', 'Initializer']
    private static final List<String> TYPES = ['TypeDeclaration', 'EnumDeclaration', 'AnnotationTypeDeclaration', 'RecordDeclaration']
    private static final List<String> ANONYMOUS_TYPES = ['AnonymousClassDeclaration', 'EnumConstantDeclaration']

    Map<String, int[]> getModifiedMethodsLines(File ancestorFile, File targetFile) {
        Map<String, Set<Integer>> lines = [:]
        for (DeclarationLine change : modifiedLines(ancestorFile, targetFile)) {
            lines.computeIfAbsent(change.signature) { new TreeSet<>() }.add(change.line)
        }
        return lines.collectEntries { signature, numbers -> [(signature): numbers as int[]] }
    }

    List<ModifiedMethod> getAllModifiedMethods(File ancestorFile, File targetFile) {
        Set<String> signatures = new LinkedHashSet<>()
        for (DeclarationLine change : modifiedLines(ancestorFile, targetFile)) {
            if (change.inTarget && change.source.isMethod(change.declaration)) {
                signatures.add(change.source.fullSignature(change.declaration))
            }
        }
        return signatures.collect { new ModifiedMethod(it) }
    }

    // A modified line, as diff numbers it, and the declaration that contains it
    private static class DeclarationLine {
        int line
        Source source
        Tree declaration
        String signature
        boolean inTarget
    }

    private List<DeclarationLine> modifiedLines(File ancestorFile, File targetFile) {
        Source ancestor = new Source(ancestorFile)
        Source target = new Source(targetFile)
        MappingStore mappings = Matchers.getInstance().getMatcher().match(ancestor.root, target.root)
        EditScript actions = new SimplifiedChawatheScriptGenerator().computeActions(mappings)

        // the lines where GumTree changed code, in the ancestor and in the target
        Set<Integer> ancestorLines = new HashSet<>()
        Set<Integer> targetLines = new HashSet<>()
        for (Action action : actions) {
            Tree node = action.getNode()
            if (inJavadoc(node)) {
                continue
            }
            if (action instanceof Insert || action instanceof TreeInsert) {
                targetLines.addAll(target.lines(node))
            } else {
                ancestorLines.addAll(ancestor.lines(node))
                if (action instanceof Update || action instanceof Move) {
                    Tree targetNode = mappings.getDstForSrc(node)
                    if (targetNode != null) {
                        targetLines.addAll(target.lines(targetNode))
                    }
                }
            }
        }

        List<DeclarationLine> result = []
        // adds the line to the declaration that contains sourceLine in the source, if there is one
        Closure<Boolean> addLine = { int line, Source source, int sourceLine ->
            Tree declaration = source.declarationAt(sourceLine)
            if (declaration == null) {
                return false
            }
            String signature = source.signature(declaration, sourceLine)
            // as diffj, a method of the target keeps the name it has in the ancestor (e.g. after a parameter is added),
            // so that the same method has the same name in the diffs of left, right and merge against the base
            Tree ancestorMethod = source.is(target) && source.isMethodOrConstructor(declaration) ? mappings.getSrcForDst(declaration) : null
            if (ancestorMethod != null) {
                signature = ancestor.signature(ancestorMethod, -1)
            }
            result.add(new DeclarationLine(line: line, source: source, declaration: declaration, signature: signature, inTarget: source.is(target)))
            return true
        }

        for (Hunk hunk : textualDiff(ancestorFile, targetFile)) {
            boolean changesCode = hunk.ancestorLines.any { ancestorLines.contains(it) } || hunk.targetLines.any { targetLines.contains(it) }
            if (!changesCode) {
                continue
            }
            if (hunk.targetLines.isEmpty()) {
                // removed lines: diff numbers them by the target line after which they were removed
                addLine(hunk.targetPosition, ancestor, hunk.ancestorLines.first())
            }
            for (int line : hunk.targetLines) {
                if (!addLine(line, target, line) && !hunk.ancestorLines.isEmpty()) {
                    addLine(line, ancestor, hunk.ancestorLines.first())
                }
            }
        }
        return result
    }

    private static boolean inJavadoc(Tree node) {
        return node.getType().name == 'Javadoc' || node.getParents().any { it.getType().name == 'Javadoc' }
    }

    // A hunk of the normal diff output, e.g. "3,4c3" or "5d4"
    private static class Hunk {
        List<Integer> ancestorLines
        List<Integer> targetLines
        int targetPosition
    }

    private static List<Hunk> textualDiff(File ancestorFile, File targetFile) {
        Process diff = ProcessRunner.runProcess('.', 'diff', ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        List<String> output = diff.getInputStream().readLines()
        List<Hunk> hunks = []
        for (String line : output) {
            def header = line =~ /^(\d+)(?:,(\d+))?([acd])(\d+)(?:,(\d+))?$/
            if (!header.matches()) {
                continue
            }
            int ancestorStart = header.group(1) as int
            int ancestorEnd = (header.group(2) ?: header.group(1)) as int
            int targetStart = header.group(4) as int
            int targetEnd = (header.group(5) ?: header.group(4)) as int
            String type = header.group(3)
            hunks << new Hunk(
                    ancestorLines: type == 'a' ? [] : (ancestorStart..ancestorEnd).toList(),
                    targetLines: type == 'd' ? [] : (targetStart..targetEnd).toList(),
                    targetPosition: targetStart)
        }
        return hunks
    }

    // A Java file parsed by GumTree, with the line of each position
    private static class Source {
        final Tree root
        final String text
        final List<Integer> lineStarts = [0]

        Source(File file) {
            text = file.getText('UTF-8')
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '\n' as char) {
                    lineStarts << i + 1
                }
            }
            root = new JdtTreeGenerator().generateFrom().string(text).getRoot()
        }

        int line(int position) {
            int index = Collections.binarySearch(lineStarts, position)
            return index >= 0 ? index + 1 : -index - 1
        }

        List<Integer> lines(Tree node) {
            return (line(node.getPos())..line(Math.max(node.getPos(), node.getEndPos() - 1))).toList()
        }

        String text(Tree node) {
            return text.substring(node.getPos(), node.getEndPos())
        }

        // the innermost method, field or initializer block of a named type with the line
        Tree declarationAt(int line) {
            Tree found = null
            for (Tree node : root.preOrder()) {
                if (DECLARATIONS.contains(node.getType().name) && !inAnonymousType(node) && lines(node).contains(line)) {
                    if (found == null || node.getLength() < found.getLength()) {
                        found = node
                    }
                }
            }
            return found
        }

        private static boolean inAnonymousType(Tree declaration) {
            Tree type = declaration.getParents().find { TYPES.contains(it.getType().name) || ANONYMOUS_TYPES.contains(it.getType().name) }
            return type != null && ANONYMOUS_TYPES.contains(type.getType().name)
        }

        boolean isMethod(Tree declaration) {
            return isMethodOrConstructor(declaration) && returnType(declaration) != null
        }

        static boolean isMethodOrConstructor(Tree declaration) {
            return declaration.getType().name == 'MethodDeclaration'
        }

        // as diffj: "m(String, int)", the field name, or "static block"
        String signature(Tree declaration, int line) {
            switch (declaration.getType().name) {
                case 'FieldDeclaration':
                    List<Tree> fragments = declaration.getChildren().findAll { it.getType().name == 'VariableDeclarationFragment' }
                    Tree fragment = fragments.find { lines(it).contains(line) } ?: fragments.first()
                    return name(fragment)
                case 'Initializer':
                    return 'static block'
                default:
                    String parameters = declaration.getChildren()
                            .findAll { it.getType().name == 'SingleVariableDeclaration' }
                            // the first type below the parameter: for varargs (int... xs) GumTree nests it, and diffj names it int
                            .collect { parameter -> typeText(parameter.preOrder().find { it != parameter && isType(it) }) }
                            .join(', ')
                    return "${name(declaration)}(${parameters})"
            }
        }

        // as diffj --changed-methods-only: "<a.C$Inner: void m(int)>", or "<a.C: <T> m(int)>" for a generic method
        String fullSignature(Tree method) {
            String types = method.getParents().findAll { TYPES.contains(it.getType().name) }.reverse().collect { name(it) }.join('$')
            List<Tree> typeParameters = method.getChildren().findAll { it.getType().name == 'TypeParameter' }
            String prefix = typeParameters ? "<${typeParameters.collect { typeText(it) }.join(', ')}>" : typeText(returnType(method))
            return "<${types}: ${prefix} ${signature(method, -1)}>"
        }

        private Tree returnType(Tree method) {
            Tree methodName = method.getChildren().find { it.getType().name == 'SimpleName' }
            List<Tree> children = method.getChildren()
            return children.subList(0, children.indexOf(methodName)).find { isType(it) }
        }

        private static String name(Tree node) {
            return node.getChildren().find { it.getType().name == 'SimpleName' }.getLabel()
        }

        private static boolean isType(Tree node) {
            return node.getType().name.endsWith('Type')
        }

        private String typeText(Tree type) {
            return text(type).replaceAll(/\s+/, '')
        }
    }
}
