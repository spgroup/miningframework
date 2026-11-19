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
 * Finds the modified methods with GumTree, which compares the syntax trees of two Java files.
 *
 * How it works:
 *   1. GumTree lists the changes in the trees; we keep the lines of code they touch.
 *   2. The textual diff (diff) lists the modified lines in blocks ("hunks").
 *   3. A hunk counts only if GumTree changed code in it, so changes only in comments or spaces are ignored.
 *   4. Each line of a hunk goes to the method, field or initializer block that contains it.
 *
 * The line numbers come from diff, so they match the ones ModifiedMethodsHelper reads from diff.
 * The names are the ones diffj gives: "m(String, int)", "TABLE", "static block".
 */
class GumTreeDiffTool implements MethodDiffTool {

    private static final List<String> DECLARATION_TYPES = ['MethodDeclaration', 'FieldDeclaration', 'Initializer']
    private static final List<String> NAMED_CLASS_TYPES = ['TypeDeclaration', 'EnumDeclaration', 'AnnotationTypeDeclaration', 'RecordDeclaration']
    private static final List<String> ANONYMOUS_CLASS_TYPES = ['AnonymousClassDeclaration', 'EnumConstantDeclaration']

    Map<String, int[]> getModifiedMethodsLines(File baseFile, File targetFile) {
        List<ModifiedLine> modifiedLines = findModifiedLines(baseFile, targetFile)

        // lines of the target first: if a statement moved inside a method, the line where it is now wins over
        // the line it was removed from (ModifiedMethodsHelper keeps one line per content)
        modifiedLines.sort { a, b -> a.inTarget != b.inTarget ? (a.inTarget ? -1 : 1) : a.line <=> b.line }

        Map<String, Set<Integer>> linesByDeclaration = [:]
        for (ModifiedLine modifiedLine : modifiedLines) {
            linesByDeclaration.computeIfAbsent(modifiedLine.declarationName) { new LinkedHashSet<>() }.add(modifiedLine.line)
        }
        return linesByDeclaration.collectEntries { name, lines -> [(name): lines as int[]] }
    }

    List<ModifiedMethod> getAllModifiedMethods(File baseFile, File targetFile) {
        Set<String> signatures = new LinkedHashSet<>()
        for (ModifiedLine modifiedLine : findModifiedLines(baseFile, targetFile)) {
            if (modifiedLine.inTarget && modifiedLine.file.isMethod(modifiedLine.declaration)) {
                signatures.add(modifiedLine.file.signatureWithClass(modifiedLine.declaration))
            }
        }
        return signatures.collect { new ModifiedMethod(it) }
    }

    // A modified line (numbered as diff numbers it) and the declaration that contains it
    private static class ModifiedLine {
        int line
        JavaFile file
        Tree declaration
        String declarationName
        boolean inTarget
    }

    private static List<ModifiedLine> findModifiedLines(File baseFile, File targetFile) {
        JavaFile base = new JavaFile(baseFile)
        JavaFile target = new JavaFile(targetFile)
        MappingStore mappings = Matchers.getInstance().getMatcher().match(base.root, target.root)
        CodeLines changedCode = findLinesChangedByGumTree(base, target, mappings)

        List<ModifiedLine> modifiedLines = []
        for (Hunk hunk : runTextualDiff(baseFile, targetFile)) {
            boolean hunkChangesCode = hunk.baseLines.any { changedCode.base.contains(it) } ||
                    hunk.targetLines.any { changedCode.target.contains(it) }
            if (!hunkChangesCode) {
                continue
            }

            if (hunk.isRemoval()) {
                // diff numbers removed lines by the target line after which they were removed
                ModifiedLine removed = createModifiedLine(hunk.targetPosition, base, hunk.baseLines.first(), base, mappings)
                if (removed != null) {
                    modifiedLines.add(removed)
                }
            }

            for (int line : hunk.targetLines) {
                ModifiedLine modifiedLine = createModifiedLine(line, target, line, base, mappings)
                // a line outside any declaration of the target: try the declaration of the base
                if (modifiedLine == null && !hunk.baseLines.isEmpty()) {
                    modifiedLine = createModifiedLine(line, base, hunk.baseLines.first(), base, mappings)
                }
                if (modifiedLine != null) {
                    modifiedLines.add(modifiedLine)
                }
            }
        }
        return modifiedLines
    }

    /*
     * Finds the declaration that contains lineInFile in file and returns the modified line, or null when the line
     * is outside any declaration.
     */
    private static ModifiedLine createModifiedLine(int line, JavaFile file, int lineInFile, JavaFile base, MappingStore mappings) {
        Tree declaration = file.findDeclarationContaining(lineInFile)
        if (declaration == null) {
            return null
        }
        String name = file.diffjName(declaration, lineInFile)

        // a method of the target keeps the name it has in the base (e.g. after a parameter is added),
        // so that left, right and merge name the same method alike
        boolean inTarget = !file.is(base)
        if (inTarget && JavaFile.isMethodOrConstructor(declaration)) {
            Tree baseMethod = mappings.getSrcForDst(declaration)
            if (baseMethod != null) {
                name = base.diffjName(baseMethod, -1)
            }
        }
        return new ModifiedLine(line: line, file: file, declaration: declaration, declarationName: name, inTarget: inTarget)
    }

    // The lines of code changed by GumTree, in the base and in the target
    private static class CodeLines {
        Set<Integer> base = new HashSet<>()
        Set<Integer> target = new HashSet<>()
    }

    private static CodeLines findLinesChangedByGumTree(JavaFile base, JavaFile target, MappingStore mappings) {
        EditScript edits = new SimplifiedChawatheScriptGenerator().computeActions(mappings)
        CodeLines changed = new CodeLines()

        for (Action edit : edits) {
            Tree node = edit.getNode()
            if (isInJavadoc(node)) {
                continue
            }

            // an inserted node only exists in the target
            if (edit instanceof Insert || edit instanceof TreeInsert) {
                changed.target.addAll(target.linesOf(node))
                continue
            }

            // a deleted, updated or moved node is in the base
            changed.base.addAll(base.linesOf(node))
            if (!(edit instanceof Update || edit instanceof Move)) {
                continue
            }

            // an updated or moved node is also in the target
            Tree targetNode = mappings.getDstForSrc(node)
            if (targetNode == null) {
                continue
            }
            changed.target.addAll(target.linesOf(targetNode))

            // a statement that moved inside the same block: diff may show the statements in between as changed
            // (e.g. two swapped statements), so they count as changed too
            boolean movedInsideTheSameParent = edit instanceof Move && mappings.getDstForSrc(node.getParent())?.is(targetNode.getParent())
            if (movedInsideTheSameParent) {
                int first = Math.min(node.positionInParent(), targetNode.positionInParent())
                int last = Math.max(node.positionInParent(), targetNode.positionInParent())
                childrenBetween(node.getParent(), first, last).each { changed.base.addAll(base.linesOf(it)) }
                childrenBetween(targetNode.getParent(), first, last).each { changed.target.addAll(target.linesOf(it)) }
            }
        }
        return changed
    }

    private static List<Tree> childrenBetween(Tree parent, int first, int last) {
        List<Tree> children = parent.getChildren()
        return children.subList(Math.min(first, children.size()), Math.min(last + 1, children.size()))
    }

    private static boolean isInJavadoc(Tree node) {
        return node.getType().name == 'Javadoc' || node.getParents().any { it.getType().name == 'Javadoc' }
    }

    // A block of the output of diff, e.g. "3,4c3" (lines 3 and 4 of the base changed into line 3 of the target)
    private static class Hunk {
        List<Integer> baseLines
        List<Integer> targetLines
        int targetPosition

        boolean isRemoval() {
            return targetLines.isEmpty()
        }
    }

    private static List<Hunk> runTextualDiff(File baseFile, File targetFile) {
        Process diff = ProcessRunner.runProcess('.', 'diff', baseFile.getAbsolutePath(), targetFile.getAbsolutePath())
        List<Hunk> hunks = []
        for (String outputLine : diff.getInputStream().readLines()) {
            // e.g. "3,4c3": base lines 3 to 4, the type (a = added, c = changed, d = deleted), target line 3
            def header = outputLine =~ /^(\d+)(?:,(\d+))?([acd])(\d+)(?:,(\d+))?$/
            if (!header.matches()) {
                continue
            }
            int baseStart = header.group(1) as int
            int baseEnd = (header.group(2) ?: header.group(1)) as int
            String type = header.group(3)
            int targetStart = header.group(4) as int
            int targetEnd = (header.group(5) ?: header.group(4)) as int

            hunks << new Hunk(
                    baseLines: type == 'a' ? [] : (baseStart..baseEnd).toList(),
                    targetLines: type == 'd' ? [] : (targetStart..targetEnd).toList(),
                    targetPosition: targetStart)
        }
        return hunks
    }

    // A Java file parsed by GumTree, which knows the line of each node
    private static class JavaFile {
        final Tree root
        final String text
        final List<Integer> lineStartPositions = [0]

        JavaFile(File file) {
            text = file.getText('UTF-8')
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '\n' as char) {
                    lineStartPositions << i + 1
                }
            }
            root = new JdtTreeGenerator().generateFrom().string(text).getRoot()
        }

        int lineOf(int position) {
            int index = Collections.binarySearch(lineStartPositions, position)
            return index >= 0 ? index + 1 : -index - 1
        }

        List<Integer> linesOf(Tree node) {
            int lastPosition = Math.max(node.getPos(), node.getEndPos() - 1)
            return (lineOf(node.getPos())..lineOf(lastPosition)).toList()
        }

        String textOf(Tree node) {
            return text.substring(node.getPos(), node.getEndPos())
        }

        // The smallest method, field or initializer block with the line, outside anonymous classes
        // (a change in an anonymous class goes to the method that creates it, as in diffj)
        Tree findDeclarationContaining(int line) {
            Tree found = null
            for (Tree node : root.preOrder()) {
                boolean isDeclaration = DECLARATION_TYPES.contains(node.getType().name)
                if (isDeclaration && !isInAnonymousClass(node) && linesOf(node).contains(line)) {
                    if (found == null || node.getLength() < found.getLength()) {
                        found = node
                    }
                }
            }
            return found
        }

        private static boolean isInAnonymousClass(Tree declaration) {
            Tree enclosingClass = declaration.getParents().find {
                NAMED_CLASS_TYPES.contains(it.getType().name) || ANONYMOUS_CLASS_TYPES.contains(it.getType().name)
            }
            return enclosingClass != null && ANONYMOUS_CLASS_TYPES.contains(enclosingClass.getType().name)
        }

        static boolean isMethodOrConstructor(Tree declaration) {
            return declaration.getType().name == 'MethodDeclaration'
        }

        boolean isMethod(Tree declaration) {
            return isMethodOrConstructor(declaration) && returnTypeOf(declaration) != null
        }

        // The name diffj gives: "m(String, int)" for a method, the name of a field, "static block" for a block
        String diffjName(Tree declaration, int line) {
            switch (declaration.getType().name) {
                case 'FieldDeclaration':
                    // in "int a = 0, b = 2;" the field is the one on the line
                    List<Tree> fields = declaration.getChildren().findAll { it.getType().name == 'VariableDeclarationFragment' }
                    Tree field = fields.find { linesOf(it).contains(line) } ?: fields.first()
                    return nameOf(field)
                case 'Initializer':
                    return 'static block'
                default:
                    List<Tree> parameters = declaration.getChildren().findAll { it.getType().name == 'SingleVariableDeclaration' }
                    return "${nameOf(declaration)}(${parameters.collect { parameterTypeOf(it) }.join(', ')})"
            }
        }

        // The type of a parameter without spaces: for "int... xs" GumTree nests the type, and diffj names it "int"
        private String parameterTypeOf(Tree parameter) {
            Tree type = parameter.preOrder().find { it != parameter && isType(it) }
            return textWithoutSpaces(type)
        }

        // As diffj --changed-methods-only: "<a.C$Inner: void m(int)>", or "<a.C: <T> m(int)>" for a generic method
        String signatureWithClass(Tree method) {
            List<Tree> classes = method.getParents().findAll { NAMED_CLASS_TYPES.contains(it.getType().name) }
            String className = classes.reverse().collect { nameOf(it) }.join('$')
            List<Tree> typeParameters = method.getChildren().findAll { it.getType().name == 'TypeParameter' }
            String returnType = typeParameters ? "<${typeParameters.collect { textWithoutSpaces(it) }.join(', ')}>" : textWithoutSpaces(returnTypeOf(method))
            return "<${className}: ${returnType} ${diffjName(method, -1)}>"
        }

        // The type before the method name; a constructor has none
        private Tree returnTypeOf(Tree method) {
            List<Tree> children = method.getChildren()
            Tree methodName = children.find { it.getType().name == 'SimpleName' }
            return children.subList(0, children.indexOf(methodName)).find { isType(it) }
        }

        private static String nameOf(Tree node) {
            return node.getChildren().find { it.getType().name == 'SimpleName' }.getLabel()
        }

        private static boolean isType(Tree node) {
            return node.getType().name.endsWith('Type')
        }

        private String textWithoutSpaces(Tree node) {
            return textOf(node).replaceAll(/\s+/, '')
        }
    }
}
